package com.inalogy.midpoint.connectors.ssh;

import com.inalogy.midpoint.connectors.utils.Constants;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;
import org.identityconnectors.framework.spi.StatefulConfiguration;

public class SshConfiguration extends AbstractConfiguration implements StatefulConfiguration {

    /**
     * Server hostname.
     */
    private String host;

    /**
     * Server port.
     */
    private int port = 22;

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

    /**
     * Defines how to handle NULL value arguments.
     * If a script argument is NULL, it can be inserted as an empty string ("asEmpty") or it can be removed from the argument list ("asGone").
     */
    private String handleNullValues = HANDLE_NULL_AS_GONE;

    public static final String HANDLE_NULL_AS_EMPTY_STRING = "asEmptyString";
    public static final String HANDLE_NULL_AS_GONE = "asGone";
    private String shellType = Constants.TYPE_SHELL;

    public String schemaFilePath;
    private String[] knownHosts;

    @Override
    public void validate() {

    }
    @ConfigurationProperty(order = 100)
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @ConfigurationProperty(order = 101)
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @ConfigurationProperty(order = 102)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @ConfigurationProperty(order = 103)
    public GuardedString getPassword() {
        return password;
    }

    public void setPassword(GuardedString password) {
        this.password = password;
    }

    @ConfigurationProperty(order = 104)
    public GuardedString getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(GuardedString privateKey) {
        this.privateKey = privateKey;
    }

    @ConfigurationProperty(order = 105)
    public GuardedString getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(GuardedString passphrase) {
        this.passphrase = passphrase;
    }

    @ConfigurationProperty(order = 106)
    public String getAuthenticationScheme() {
        return authenticationScheme;
    }

    public void setAuthenticationScheme(String authenticationScheme) {
        this.authenticationScheme = authenticationScheme;
    }

    @ConfigurationProperty(order = 110)
    public String[] getKnownHosts() {
        return knownHosts;
    }

    public void setKnownHosts(String[] knownHosts) {
        this.knownHosts = knownHosts;
    }

    @ConfigurationProperty(order = 120)
    public String getArgumentStyle() {
        return argumentStyle;
    }

    public void setArgumentStyle(String argumentStyle) {
        this.argumentStyle = argumentStyle;
    }

    @ConfigurationProperty(order = 130)
    public String getHandleNullValues() {
        return handleNullValues;
    }

    public void setHandleNullValues(String handleNullValues) {
        this.handleNullValues = handleNullValues;
    }

    @ConfigurationProperty(
            order = 100,
            displayMessageKey = "shell.type.display",
            groupMessageKey = "basic.group",
            helpMessageKey = "shell.type.help"
    )
    public String getShellType() {
        return shellType;
    }

    public void setShellType(String shellType) {
        this.shellType = shellType;
    }
    @ConfigurationProperty(
            displayMessageKey = "schema.file.path"
    )
    public String getSchemaFilePath() {return schemaFilePath;}
    public void setSchemaFilePath(String schemaFilePath){this.schemaFilePath = schemaFilePath;}


    @Override
    public void release() {

    }
}
