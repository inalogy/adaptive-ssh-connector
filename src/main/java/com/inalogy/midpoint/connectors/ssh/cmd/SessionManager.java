package com.inalogy.midpoint.connectors.ssh.cmd;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.inalogy.midpoint.connectors.ssh.AdaptiveSshConfiguration;
import com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig.DynamicConfiguration;
import com.inalogy.midpoint.connectors.ssh.utils.Constants;

import com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig.FlagSettings;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.ConnectionFailedException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;

import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;

public class SessionManager {
    private Session.Shell shell = null;
    private BufferedReader shellReader = null;
    private OutputStream shellWriter = null;
    private final AdaptiveSshConfiguration configuration;
    private SSHClient ssh;
    private Session session = null;
    private final HostKeyVerifier hostKeyVerifier;
    private final AuthenticationManager authManager;
    private static final Log LOG = Log.getLog(SessionManager.class);
    private DynamicConfiguration dynamicConfiguration;

    public SessionManager(AdaptiveSshConfiguration configuration, DynamicConfiguration dynamicConfiguration) {
        this.configuration = configuration;
        this.hostKeyVerifier = new ConnectorKnownHostsVerifier().parse(this.configuration.getKnownHosts());
        this.authManager = new AuthenticationManager(this.configuration);
        this.dynamicConfiguration = dynamicConfiguration;
    }

    public String exec(String processedCommand) {
        if (configuration.isUsePersistentShell()) {
            return execViaShell(processedCommand);
        } else {
            return execViaExec(processedCommand);
        }
    }

    private String execViaExec(String processedCommand) {
        startSession();
        final Session.Command cmd;
        try {
            cmd = session.exec(processedCommand);
        } catch (ConnectionException | TransportException e) {
            throw new ConnectorIOException("Network error while executing SSH command: "+e.getMessage(), e);
        }
        String output;
        String error;
        try {
            LOG.ok("---- executing ssh command -----------");
            LOG.ok("processedCommand: {0} ", processedCommand);
            output = IOUtils.readFully(cmd.getInputStream()).toString();
            LOG.ok("SSH command output: {0}", output);
            error = IOUtils.readFully(cmd.getErrorStream()).toString();
            LOG.ok("SSH command error: {0}", error);
            LOG.ok("command error: {0}", error);
            LOG.ok("command exitErrorMsg: {0}", cmd.getExitErrorMessage());
            LOG.ok("command exitStatus: {0}", cmd.getExitStatus());
            LOG.ok("command exitSignal: {0}", cmd.getExitSignal());
            LOG.ok("--------------------------------------");

            if (!error.isEmpty()){
                throw new ConnectorException("Error executing SSH command: " + error);
            }
        } catch (IOException e) {
            throw new ConnectorIOException("Error reading output of SSH command: "+e.getMessage(), e);
        }

        try {
            cmd.join(configuration.getSshResponseTimeout(), TimeUnit.SECONDS);
        } catch (ConnectionException e) {
            throw new ConnectorIOException("Error \"joining\" SSH command: "+e.getMessage(), e);
        }

        LOG.ok("SSH command exit status: {0}", cmd.getExitStatus());
        closeSession();
        handleErrors(output);
        return output;
    }

    private String execViaShell(String processedCommand) {
        startSession();
        try {
            String output = runShellWithMarkers(processedCommand, configuration.getSshResponseTimeout());

            handleErrors(output);
            return output;

        } catch (TimeoutException e) {
            closeSession();
            LOG.error("SSH shell command timed out. Closing session");
            throw new ConnectorIOException("SSH shell command timed out after "
                    + configuration.getSshResponseTimeout() + " seconds", e);
        } catch (Exception e) {
            closeSession();
            LOG.error("Unexpected error while execViaShell {0}", e.getMessage());
            throw new ConnectorIOException("Error executing command via shell: " + e.getMessage(), e);
        }
    }


    private String runShellWithMarkers(String processedCommand, int timeoutSeconds) throws Exception {
        String newLineSeparator = this.dynamicConfiguration.getSettings()
                .getScriptResponseSettings()
                .getResponseNewLineSeparator();

        String uuid = UUID.randomUUID().toString();
        String startMarker = "__COMMAND_START__" + uuid;
        String endMarker = "__COMMAND_DONE__" + uuid;

        String finalCommand = "echo " + startMarker + " ; " + processedCommand + " ; echo " + endMarker + "\r\n";
        //reconsider how expired sessions should be handled?!Custom error flag to reinit preloadScript? or kill session?
        // or kill session on GeneralFatalError?
        shellWriter.write(finalCommand.getBytes(StandardCharsets.UTF_8));
        shellWriter.flush();

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Callable<String> shellReadTask = () -> {
                StringBuilder outputBuilder = new StringBuilder();
                boolean insideCommandOutput = false;
                String line;

                while ((line = shellReader.readLine()) != null) {
                    LOG.ok("SHELL LINE: {0}", line);

                    if (line.contains(startMarker)) {
                        insideCommandOutput = true;
                        continue;
                    }

                    if (line.contains(endMarker)) {
                        int markerIndex = line.indexOf(endMarker);
                        outputBuilder.append(line, 0, markerIndex);
                        break;
                    }

                    if (insideCommandOutput) {
                        outputBuilder.append(line).append(newLineSeparator);
                    }
                }

                return outputBuilder.toString().trim();
            };

            Future<String> future = executor.submit(shellReadTask);
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        }
    }





    public void initSshClient(){
        if (ssh == null || !ssh.isConnected()) {
            DefaultConfig defaultConfig = new DefaultConfig();
            defaultConfig.setKeepAliveProvider(KeepAliveProvider.KEEP_ALIVE);
            ssh = new SSHClient(defaultConfig);
            ssh.getConnection().getKeepAlive().setKeepAliveInterval(Constants.SSH_CLIENT_KEEP_ALIVE_INTERVAL);
            ssh.addHostKeyVerifier(hostKeyVerifier);
            try {
                ssh.connect(configuration.getHost(), configuration.getPort());
                LOG.info("Connecting to {0}", authManager.getConnectionDesc());
            } catch (IOException e) {
                LOG.error("Error creating SSH connection to {0}: {1}", authManager.getHostDesc(), e.getMessage());
                throw new ConnectionFailedException("Error creating SSH connection to " + authManager.getHostDesc() + ": " + e.getMessage(), e);
            }
        } else {
            LOG.ok("reusing Ssh client");
        }
    }

    public void handleErrors(String rawOutput) {
     String fatalErrorMsg = dynamicConfiguration.getSettings().getSearchOperationSettings().getGeneralFatalErrorMessage();
        if (fatalErrorMsg != null && !fatalErrorMsg.isEmpty() && rawOutput.contains(fatalErrorMsg)) {
            LOG.error("Fatal error in response: {0}", rawOutput);
            if (this.configuration.isUsePersistentShell()){
                closeSession();
            }
            throw new ConnectorException("Fatal error in response: " + rawOutput);
        }
    }

    public boolean isConnectionAlive(){
        return ssh != null && ssh.isConnected();
    }

    public void disposeSshClient(){
        if (ssh != null && ssh.isConnected()){
            try {
                LOG.info("Disposing SSHClient");
                ssh.disconnect();
            } catch (IOException e) {
                LOG.error("Exception occurred while disposing SSHClient: " + e);
            }
        }
    }

    public void startSession() {
        initSshClient();
        if (ssh != null && ssh.isConnected() && !ssh.isAuthenticated()) {
            authManager.authenticate(ssh);
        }
        try {
            if (ssh != null && ssh.isConnected() && session == null) {
                session = ssh.startSession();
                if (configuration.isUsePersistentShell()) {

                    shell = session.startShell();
                    shellWriter = shell.getOutputStream();
                    shellReader = new BufferedReader(new InputStreamReader(shell.getInputStream()));

                    String preloadScriptPath = "";
                    FlagSettings preloadScriptSettings = this.dynamicConfiguration.getSettings().getConnectorSettings().getPreloadScript();
                    if (preloadScriptSettings != null && preloadScriptSettings.isEnabled()){
                        preloadScriptPath = preloadScriptSettings.getValue();
                        if (preloadScriptPath != null && !preloadScriptPath.isEmpty()){
                            executePreloadScript(preloadScriptPath);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new ConnectionFailedException("Error starting SSH session/shell: " + e.getMessage(), e);
        }
    }

    private void executePreloadScript(String preloadScriptPath) {
        if (preloadScriptPath != null && !preloadScriptPath.isEmpty()) {
            String command = ". '" + preloadScriptPath + "'";
            try {
                String output = runShellWithMarkers(command, configuration.getSshResponseTimeout());

                if (!output.isEmpty()) {
                    LOG.error("Preload script produced unexpected output:\n{0}", output);
                    throw new ConnectorIOException("Unexpected output during preload script execution:\n" + output +
                            "preloadScript must produce NO OUTPUT if execution was successful");
                } else {
                    LOG.ok("Preload script executed successfully: {0}", preloadScriptPath);
                }

            } catch (Exception e) {
                LOG.error("Failed to execute preload script: {0} {1}", e.getMessage(), e);
                throw new ConnectionFailedException("Failed to execute preload script: " + e.getMessage(), e);
            }
        }
    }


    public void closeSession() {
        if ((ssh.isConnected() && session != null && session.isOpen()) || configuration.isUsePersistentShell()) {
            LOG.ok("Disconnecting from {0}", authManager.getConnectionDesc());
            try {
                if (session != null) {
                    session.close();
                }
            } catch (ConnectionException | TransportException e) {
                LOG.warn("Error closing SSH session for {0}: {1} (ignoring)", authManager.getConnectionDesc(), e.getMessage());
            }
            LOG.ok("Connection to {0} disconnected", authManager.getConnectionDesc());
        }
    }
}
