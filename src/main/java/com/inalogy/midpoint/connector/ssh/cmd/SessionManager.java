package com.inalogy.midpoint.connector.ssh.cmd;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.inalogy.midpoint.connector.ssh.SshConfiguration;
import com.inalogy.midpoint.connector.ssh.utils.dynamicconfig.DynamicConfiguration;
import com.inalogy.midpoint.connector.ssh.utils.Constants;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.ConnectionFailedException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;

import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;

/**
 * This class is responsible for Initializing and establishing Ssh connection and Ssh session spawning, Execution of commands.
 */
public class SessionManager {

    private final SshConfiguration configuration;
    private SSHClient ssh;
    private Session session = null;
    private final HostKeyVerifier hostKeyVerifier;
    private final AuthenticationManager authManager;
    private static final Log LOG = Log.getLog(SessionManager.class);
    private DynamicConfiguration dynamicConfiguration;

    public SessionManager(SshConfiguration configuration, DynamicConfiguration dynamicConfiguration) {
        this.configuration = configuration;
        this.hostKeyVerifier = new ConnectorKnownHostsVerifier().parse(this.configuration.getKnownHosts());
        this.authManager = new AuthenticationManager(this.configuration);
        this.dynamicConfiguration = dynamicConfiguration;

    }

    /**
     *
     * @param processedCommand e.g. C:/Users/test/Desktop/searchScript.ps1 -ExchangeGuid "someguid"
     * @return Response as String
     */
    public String exec(String processedCommand) {
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

            //throwing Exception based on exitStatus (e.g. !Integer.valueOf(0).equals(cmd.getExitStatus()) ) was not feasible
            // - calling powershell successfully returned exitCode null
            // - there may be return codes <> 0 having empty error stream. E.g. calling grep (linux) having empty result
            // simple solution: throw Exception if there is something in error stream
            if (!error.isEmpty()){
                LOG.error("---- error executing ssh command ----");
                LOG.error("-- processedCommand: {0} ", processedCommand);
                LOG.error("-- command output: {0}", output);
                LOG.error("-- command error: {0}", error);
                LOG.error("-- command exitErrorMsg: {0}", cmd.getExitErrorMessage());
                LOG.error("-- command exitStatus: {0}", cmd.getExitStatus());
                LOG.error("-- command exitSignal: {0}", cmd.getExitSignal());
                LOG.error("--------------------------------------");
                handleErrors(error);
                throw new ConnectorException("Error executing SSH command: " + error);
            }
        } catch (IOException e) {
            throw new ConnectorIOException("Error reading output of SSH command: "+e.getMessage(), e);
        }

        try {
            cmd.join(Constants.SSH_RESPONSE_TIMEOUT, TimeUnit.SECONDS);
        } catch (ConnectionException e) {
            throw new ConnectorIOException("Error \"joining\" SSH command: "+e.getMessage(), e);
        }

        LOG.ok("SSH command exit status: {0}", cmd.getExitStatus());
        closeSession();
        handleErrors(output);
        return output;
    }

    /**
     * Establish connection with remote SSH Server.
     * Single SshClient connection can handle multiple Sessions
     * For maximum performance we need to keep SshClient alive, so we prevent establishing connection for every request
     */
    public void initSshClient(){

        if (ssh == null || !ssh.isConnected()) {

            DefaultConfig defaultConfig = new DefaultConfig();
            defaultConfig.setKeepAliveProvider(KeepAliveProvider.KEEP_ALIVE);
            ssh = new SSHClient(defaultConfig);

            // Keep Alive must be supported by ssh server
            ssh.getConnection().getKeepAlive().setKeepAliveInterval(Constants.SSH_CLIENT_KEEP_ALIVE_INTERVAL);

            ssh.addHostKeyVerifier(hostKeyVerifier);

            try {
                // connect should occur only once till connection is dropped
                ssh.connect(configuration.getHost(), configuration.getPort());
                LOG.info("Connecting to {0}", authManager.getConnectionDesc());
            } catch (IOException e) {
                LOG.error("Error creating SSH connection to {0}: {1}", authManager.getHostDesc(), e.getMessage());
                throw new ConnectionFailedException("Error creating SSH connection to " + authManager.getHostDesc() + ": " + e.getMessage(), e);
            }
        }
        else {
            LOG.ok("reusing Ssh client");
        }
    }

    public void handleErrors(String rawOutput){
        String ALREADY_EXISTS_ERROR_RESPONSE = this.dynamicConfiguration.getSettings().getCreateOperationSettings().getAlreadyExistsErrorParameter();
        String OBJECT_NOT_FOUND_ERROR_RESPONSE = this.dynamicConfiguration.getSettings().getUpdateOperationSettings().getUnknownUidException();

        if (rawOutput.contains(ALREADY_EXISTS_ERROR_RESPONSE)){
            throw new AlreadyExistsException(rawOutput);
        } else if (rawOutput.contains(OBJECT_NOT_FOUND_ERROR_RESPONSE)) {
            throw new UnknownUidException(rawOutput);
        }

    }

    public boolean isConnectionAlive(){
        return ssh != null && ssh.isConnected();
    }

    /**
     * Dispose SshClient and close connection when connector is disposed
     */
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

    /**
     * For every request we need to create new session
     */
    public void startSession() {
        initSshClient();
        if (ssh != null && ssh.isConnected() && !ssh.isAuthenticated()) {
            authManager.authenticate(ssh);
        }
        try {
            if (ssh != null && ssh.isConnected()){
                session = ssh.startSession();
            }

        } catch (ConnectionException | TransportException e) {
            LOG.error("Communication error while creating SSH session for {1} failed: {2}", authManager.getConnectionDesc(), e.getMessage());
            throw new ConnectionFailedException("Communication error while creating SSH session for "+authManager.getConnectionDesc()+" failed: " + e.getMessage(), e);
        }
        LOG.ok("Session Started: {0}", authManager.getConnectionDesc());
    }

    /**
     * Every session can be used to execute only single command, after each exec we need to closeSession
     */
    public void closeSession(){
        if (ssh.isConnected() && session.isOpen()) {
            LOG.ok("Disconnecting from {0}", authManager.getConnectionDesc());
            try {
                session.close();
            } catch (ConnectionException | TransportException e) {
                LOG.warn("Error closing SSH session for {0}: {1} (ignoring)", authManager.getConnectionDesc(), e.getMessage());
            }
            LOG.ok("Connection to {0} disconnected", authManager.getConnectionDesc());
        }
    }

}
