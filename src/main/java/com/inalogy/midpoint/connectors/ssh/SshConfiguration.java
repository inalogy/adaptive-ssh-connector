package com.inalogy.midpoint.connectors.ssh;

import org.identityconnectors.framework.spi.ConfigurationProperty;

public class SshConfiguration extends com.evolveum.polygon.connector.ssh.SshConfiguration {

    public static final String TYPE_POWERSHELL = "powershell";
    public static final String TYPE_SHELL = "shell";
    private String shellType = TYPE_SHELL;

    public SshConfiguration() {
        super();
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
}
