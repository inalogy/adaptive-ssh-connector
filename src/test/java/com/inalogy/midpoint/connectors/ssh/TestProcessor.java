package com.inalogy.midpoint.connectors.ssh;


import org.identityconnectors.common.security.GuardedString;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestProcessor {
    private SshConfiguration configuration;
    private SshConnector connector;
    private final Properties properties = new Properties();

    protected TestProcessor() {
        try {
            this.loadProperties();
            this.initConfiguration();
            this.initConnector();
        } catch (IOException e) {
            System.err.println("Problem in initialization of connector");
        }
    }

    private void loadProperties() throws IOException {
        InputStream inputStream = TestProcessor.class.getClassLoader().getResourceAsStream("test.properties");

        if (inputStream == null) {
            throw new IOException("Sorry, unable to find test prop file");
        }

        properties.load(inputStream);
    }

    private void initConfiguration() {
        configuration = new SshConfiguration();
        configuration.setUsername(properties.getProperty("username"));
        configuration.setPassword(new GuardedString(properties.getProperty("password").toCharArray()));
        configuration.setHost(properties.getProperty("host"));
        configuration.setShellType(properties.getProperty("shellType"));
        configuration.setArgumentStyle(properties.getProperty("argumentStyle"));

        String relativeSchemaFilePath = properties.getProperty("schemaFilePath");
        String absoluteSchemaFilePath = System.getProperty("user.dir") + "/"+ relativeSchemaFilePath;
        configuration.setSchemaFilePath(absoluteSchemaFilePath);
    }

    private void initConnector() {
        connector = new SshConnector();
        connector.init(configuration);
    }

    protected SshConnector getConnector() {
        return connector;
    }

    protected SshConfiguration getConfiguration() {
        return configuration;
    }

    protected Properties getProperties() {
        return properties;
    }
}