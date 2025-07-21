package com.inalogy.midpoint.connectors.ssh;

import com.inalogy.midpoint.connectors.ssh.utils.Constants;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;
import org.identityconnectors.framework.spi.StatefulConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class AdaptiveSshConfiguration extends AbstractConfiguration implements StatefulConfiguration {

    /**
     * Server hostname.
     */
    private String host;

    /**
     * Server port.
     */
    private int port = 22;

    private int sshResponseTimeout = 15;

    /**
     * Username of the user, used for authentication.
     */
    private String username;

    /**
     * User password.
     */
    private GuardedString password = null;

    /**
     * Private Key for public key authentication.
     * The format is PKCS8.
     */
    private String privateKeyFilePath;

    /**
     * Passphrase for public key authentication.
     */
    private GuardedString passphrase = null;

    private String authenticationScheme = AUTHENTICATION_SCHEME_PASSWORD;

    public static final String AUTHENTICATION_SCHEME_PASSWORD = "password";
    public static final String AUTHENTICATION_SCHEME_PUBLIC_KEY = "publicKey";
    private String argumentStyle = ARGUMENT_STYLE_DASH;

    // command -f foo -b bar
    public static final String ARGUMENT_STYLE_DASH = "dash";

    // command --fu=foo --bar=baz
//    public static final String ARGUMENT_STYLE_DASHDASH = "dashdash";

    // command /f foo /b bar
    public static final String ARGUMENT_STYLE_SLASH = "slash";

    // $fu='foo'; $bar='baz'; command $foo $bar
    public static final String ARGUMENT_STYLE_VARIABLES_POWERSHELL = "variables-powershell";

    // fu='foo'; bar='baz'; command $foo $bar
    public static final String ARGUMENT_STYLE_VARIABLES_BASH = "variables-bash";

    private String shellType = Constants.TYPE_SHELL;

    public String schemaFilePath;

    public String dynamicConfigurationFilePath;
    private String[] knownHosts;
    private boolean isUsePersistentShell = false;
    @Override
    public void validate() {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("Host must be provided.");
        }

        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be in range 1-65535.");
        }

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must be provided.");
        }

        if (schemaFilePath == null || schemaFilePath.isBlank()) {
            throw new IllegalArgumentException("Schema file path must be provided.");
        }

        File schemaFile = new File(schemaFilePath);
        if (!schemaFile.exists() || !schemaFile.isFile()) {
            throw new IllegalArgumentException("Schema file not found: " + schemaFile.getPath());
        }

        if (dynamicConfigurationFilePath == null || dynamicConfigurationFilePath.isBlank()) {
            throw new IllegalArgumentException("Dynamic configuration file path must be provided.");
        }

        File configFile = new File(dynamicConfigurationFilePath);
        if (!configFile.exists() || !configFile.isFile()) {
            throw new IllegalArgumentException("Dynamic configuration file not found: " + configFile.getPath());
        }

        File dynamicConfig = new File(dynamicConfigurationFilePath);
        if (!dynamicConfig.exists() || !dynamicConfig.isFile()) {
            throw new IllegalArgumentException("Dynamic configuration file not found: " + configFile.getPath());
        }

        if (AUTHENTICATION_SCHEME_PASSWORD.equalsIgnoreCase(authenticationScheme)) {
            if (password == null) {
                throw new IllegalArgumentException("Password must be provided when using password authentication.");
            }

        } else if (AUTHENTICATION_SCHEME_PUBLIC_KEY.equalsIgnoreCase(authenticationScheme)) {
            if (privateKeyFilePath == null || privateKeyFilePath.isBlank()) {
                throw new IllegalArgumentException("Private key file path must be provided for public key authentication.");
            }

            File keyFile = new File(privateKeyFilePath);
            if (!keyFile.exists() || !keyFile.isFile()) {
                throw new IllegalArgumentException("Private key file not found: " + keyFile.getPath());
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(keyFile))) {
                String firstLine = reader.readLine();
                if (firstLine == null || !firstLine.startsWith("-----BEGIN")) {
                    throw new IllegalArgumentException("Private key file does not appear to be a valid PEM file: " + keyFile.getPath());
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to read private key file: " + keyFile.getPath(), e);
            }

        } else {
            throw new IllegalArgumentException("Unsupported authentication scheme: " + authenticationScheme);
        }
    }


    @ConfigurationProperty(order = 10,
            displayMessageKey = "SchemaFilePath.display",
            helpMessageKey = "SchemaFilePath.help")
    public String getSchemaFilePath() {
        return schemaFilePath;
    }

    public void setSchemaFilePath(String schemaFilePath) {
        this.schemaFilePath = schemaFilePath;
    }

    @ConfigurationProperty(order = 20,
            displayMessageKey = "DynamicConfigurationFilePath.display",
            helpMessageKey = "DynamicConfigurationFilePath.help")
    public String getDynamicConfigurationFilePath() {
        return dynamicConfigurationFilePath;
    }

    public void setDynamicConfigurationFilePath(String dynamicConfigurationFilePath) {
        this.dynamicConfigurationFilePath = dynamicConfigurationFilePath;
    }

    @ConfigurationProperty(order = 70,
            displayMessageKey = "ShellType.display",
            helpMessageKey = "ShellType.help")
    public String getShellType() {
        return shellType;
    }

    public void setShellType(String shellType) {
        this.shellType = shellType;
    }

    @ConfigurationProperty(order = 100,
            displayMessageKey = "Host.display",
            helpMessageKey = "Host.help")
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @ConfigurationProperty(order = 110,
            displayMessageKey = "Port.display",
            helpMessageKey = "Port.help")
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @ConfigurationProperty(order = 130,
            displayMessageKey = "Username.display",
            helpMessageKey = "Username.help")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @ConfigurationProperty(order = 140,
            displayMessageKey = "Password.display",
            helpMessageKey = "Password.help")
    public GuardedString getPassword() {
        return password;
    }

    public void setPassword(GuardedString password) {
        this.password = password;
    }

    @ConfigurationProperty(order = 150,
            displayMessageKey = "PrivateKeyFilePath.display",
            helpMessageKey = "PrivateKeyFilePath.help")
    public String getPrivateKeyFilePath() {
        return privateKeyFilePath;
    }

    public void setPrivateKeyFilePath(String privateKeyFilePath) {
        this.privateKeyFilePath = privateKeyFilePath;
    }

    @ConfigurationProperty(order = 160,
            displayMessageKey = "Passphrase.display",
            helpMessageKey = "Passphrase.help")
    public GuardedString getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(GuardedString passphrase) {
        this.passphrase = passphrase;
    }

    @ConfigurationProperty(order = 170,
            displayMessageKey = "AuthenticationScheme.display",
            helpMessageKey = "AuthenticationScheme.help")
    public String getAuthenticationScheme() {
        return authenticationScheme;
    }

    public void setAuthenticationScheme(String authenticationScheme) {
        this.authenticationScheme = authenticationScheme;
    }

    @ConfigurationProperty(order = 180,
            displayMessageKey = "KnownHosts.display",
            helpMessageKey = "KnownHosts.help")
    public String[] getKnownHosts() {
        return knownHosts;
    }

    public void setKnownHosts(String[] knownHosts) {
        this.knownHosts = knownHosts;
    }

    @ConfigurationProperty(order = 185,
            displayMessageKey = "ArgumentStyle.display",
            helpMessageKey = "ArgumentStyle.help")
    public String getArgumentStyle() {
        return argumentStyle;
    }

    public void setArgumentStyle(String argumentStyle) {
        this.argumentStyle = argumentStyle;
    }

    @ConfigurationProperty(order = 190,
            displayMessageKey = "SshResponseTimeout.display",
            helpMessageKey = "SshResponseTimeout.help")
    public int getSshResponseTimeout() {
        return sshResponseTimeout;
    }

    public void setSshResponseTimeout(int sshResponseTimeout) {
        this.sshResponseTimeout = sshResponseTimeout;
    }
    @ConfigurationProperty(order = 190,
            displayMessageKey = "isUsePersistentShell.display",
            helpMessageKey = "isUsePersistentShell.help")
    public boolean isUsePersistentShell() {
        return isUsePersistentShell;
    }

    public void setUsePersistentShell(boolean setUsePersistentShell) {
        this.isUsePersistentShell = setUsePersistentShell;
    }



    @Override
    public void release() {
    }

}
