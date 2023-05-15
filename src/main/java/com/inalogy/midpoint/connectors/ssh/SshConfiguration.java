package com.inalogy.midpoint.connectors.ssh;

import com.inalogy.midpoint.connectors.utils.Constants;
import com.sun.org.apache.bcel.internal.Const;
import org.identityconnectors.framework.spi.ConfigurationProperty;

public class SshConfiguration extends com.evolveum.polygon.connector.ssh.SshConfiguration {


    private String shellType = Constants.TYPE_SHELL;

    public String schemaFilePath;

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
    @ConfigurationProperty(
            displayMessageKey = "schema.file.path"
    )
    public void setSchemaFilePath(String schemaFilePath){this.schemaFilePath = schemaFilePath;}
    public String getSchemaFilePath() {return schemaFilePath;}
}
