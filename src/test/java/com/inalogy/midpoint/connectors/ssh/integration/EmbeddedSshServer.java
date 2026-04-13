package com.inalogy.midpoint.connectors.ssh.integration;

import org.apache.sshd.common.channel.PtyMode;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ShellFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class EmbeddedSshServer {

    public static final String TEST_USER = "testuser";
    public static final String TEST_PASS = "testpass";

    private SshServer sshd;
    private int port;
    private final Map<String, Function<String, String>> commandHandlers = new ConcurrentHashMap<>();
    private String lastReceivedCommand;

    public void registerCommandHandler(String commandContains, Function<String, String> handler) {
        commandHandlers.put(commandContains, handler);
    }

    public void clearHandlers() {
        commandHandlers.clear();
    }

    public String getLastReceivedCommand() {
        return lastReceivedCommand;
    }

    public void start() throws IOException {
        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(0);

        // In-memory host key
        var keyPath = Files.createTempFile("hostkey", ".ser");
        keyPath.toFile().deleteOnExit();
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(keyPath));

        // Password auth
        sshd.setPasswordAuthenticator((username, password, session) ->
                TEST_USER.equals(username) && TEST_PASS.equals(password));

        // Exec mode: CommandFactory
        sshd.setCommandFactory((channelSession, command) -> new InMemoryCommand(command));

        // Shell mode: ShellFactory for persistent shell
        sshd.setShellFactory(new ShellFactory() {
            @Override
            public Command createShell(ChannelSession channel) {
                return new InteractiveShellCommand();
            }
        });

        sshd.start();
        port = sshd.getPort();
    }

    public int getPort() {
        return port;
    }

    public void stop() throws IOException {
        if (sshd != null) {
            sshd.stop(true);
        }
    }

    private String findResponse(String command) {
        lastReceivedCommand = command;
        for (Map.Entry<String, Function<String, String>> entry : commandHandlers.entrySet()) {
            if (command.contains(entry.getKey())) {
                return entry.getValue().apply(command);
            }
        }
        // Default: return empty string (success for update/delete)
        return "";
    }

    private class InMemoryCommand implements Command {
        private final String command;
        private OutputStream out;
        private OutputStream err;
        private ExitCallback exitCallback;

        InMemoryCommand(String command) {
            this.command = command;
        }

        @Override
        public void setInputStream(InputStream in) {}

        @Override
        public void setOutputStream(OutputStream out) {
            this.out = out;
        }

        @Override
        public void setErrorStream(OutputStream err) {
            this.err = err;
        }

        @Override
        public void setExitCallback(ExitCallback callback) {
            this.exitCallback = callback;
        }

        @Override
        public void start(ChannelSession channel, Environment env) throws IOException {
            try {
                String response = findResponse(command);
                out.write(response.getBytes(StandardCharsets.UTF_8));
                out.flush();
                exitCallback.onExit(0);
            } catch (Exception e) {
                try {
                    err.write(("Error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
                    err.flush();
                } catch (IOException ignored) {}
                exitCallback.onExit(1);
            }
        }

        @Override
        public void destroy(ChannelSession channel) {}
    }

    private class InteractiveShellCommand implements Command {
        private InputStream in;
        private OutputStream out;
        private OutputStream err;
        private ExitCallback exitCallback;
        private Thread shellThread;

        @Override
        public void setInputStream(InputStream in) {
            this.in = in;
        }

        @Override
        public void setOutputStream(OutputStream out) {
            this.out = out;
        }

        @Override
        public void setErrorStream(OutputStream err) {
            this.err = err;
        }

        @Override
        public void setExitCallback(ExitCallback callback) {
            this.exitCallback = callback;
        }

        @Override
        public void start(ChannelSession channel, Environment env) {
            shellThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty()) continue;

                        // Parse the compound command:
                        // echo __COMMAND_START__<uuid> ; <actual command> ; echo __COMMAND_DONE__<uuid>
                        String startMarker = null;
                        String endMarker = null;
                        String actualCommand = null;

                        if (line.contains("__COMMAND_START__") && line.contains("__COMMAND_DONE__")) {
                            // Parse: echo <startMarker> ; <command> ; echo <endMarker>
                            String[] parts = line.split(";");
                            if (parts.length >= 3) {
                                startMarker = parts[0].trim().replace("echo ", "").trim();
                                endMarker = parts[parts.length - 1].trim().replace("echo ", "").trim();
                                // The command is everything between first and last parts
                                StringBuilder cmdBuilder = new StringBuilder();
                                for (int i = 1; i < parts.length - 1; i++) {
                                    if (cmdBuilder.length() > 0) cmdBuilder.append(";");
                                    cmdBuilder.append(parts[i].trim());
                                }
                                actualCommand = cmdBuilder.toString().trim();
                            }
                        }

                        if (startMarker != null && endMarker != null && actualCommand != null) {
                            String response = findResponse(actualCommand);
                            // Write: start marker, response, end marker
                            out.write((startMarker + "\n").getBytes(StandardCharsets.UTF_8));
                            if (!response.isEmpty()) {
                                out.write((response + "\n").getBytes(StandardCharsets.UTF_8));
                            }
                            out.write((endMarker + "\n").getBytes(StandardCharsets.UTF_8));
                            out.flush();
                        } else {
                            // Not a marker-based command — just execute and return
                            String response = findResponse(line);
                            out.write((response + "\n").getBytes(StandardCharsets.UTF_8));
                            out.flush();
                        }
                    }
                } catch (IOException e) {
                    // Stream closed — normal shutdown
                }
                exitCallback.onExit(0);
            }, "embedded-ssh-shell");
            shellThread.setDaemon(true);
            shellThread.start();
        }

        @Override
        public void destroy(ChannelSession channel) {
            if (shellThread != null) {
                shellThread.interrupt();
            }
        }
    }
}
