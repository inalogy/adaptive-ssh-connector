package com.inalogy.midpoint.connectors.ssh;

import com.inalogy.midpoint.connectors.ssh.utils.Constants;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;
import org.identityconnectors.framework.spi.StatefulConfiguration;

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
    private GuardedString privateKey = null;

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

    @Override
    public void validate() {
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
            displayMessageKey = "PrivateKey.display",
            helpMessageKey = "PrivateKey.help")
    public GuardedString getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(GuardedString privateKey) {
        this.privateKey = privateKey;
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

    @Override
    public void release() {
    }

}
