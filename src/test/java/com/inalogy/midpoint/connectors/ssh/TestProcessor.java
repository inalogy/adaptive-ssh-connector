package com.inalogy.midpoint.connectors.ssh;


import com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig.DynamicConfiguration;
import org.identityconnectors.common.security.GuardedString;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class TestProcessor {
    private AdaptiveSshConfiguration configuration;
    private AdaptiveSshConnector connector;
    private final Properties properties = new Properties();

    protected final DynamicConfiguration dynamicConfiguration = DynamicConfiguration.getInstance();

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
        validateMandatoryAttributes();
        configuration = new AdaptiveSshConfiguration();
        configuration.setUsername(properties.getProperty("username"));
        configuration.setPassword(new GuardedString(properties.getProperty("password").toCharArray()));
        configuration.setHost(properties.getProperty("host"));
        configuration.setShellType(properties.getProperty("shellType"));
        configuration.setArgumentStyle(properties.getProperty("argumentStyle"));

        String relativeSchemaFilePath = properties.getProperty("schemaFilePath");
        String absoluteSchemaFilePath = System.getProperty("user.dir") + "/"+ relativeSchemaFilePath;
        configuration.setSchemaFilePath(absoluteSchemaFilePath);
        String relativeConfigFilePath = properties.getProperty("dynamicConfigFilePath");
        String absoluteConfigFilePath = System.getProperty("user.dir") + "/"+ relativeConfigFilePath;
        configuration.setDynamicConfigurationFilePath(absoluteConfigFilePath);
        dynamicConfiguration.init(this.configuration.getDynamicConfigurationFilePath());
    }

    private void validateMandatoryAttributes(){
        if (Objects.equals(properties.getProperty("username"), "")){
            throw new RuntimeException("Username is mandatory for running tests, please fill it in test.properties, or use -DskipTests=true");
        }
        if (Objects.equals(properties.getProperty("password"), "")){
            throw new RuntimeException("Password is mandatory for running tests, please fill it in test.properties, or use -DskipTests=true");
        }
    }
    private void initConnector() {
        connector = new AdaptiveSshConnector();
        connector.init(configuration);
    }

    protected AdaptiveSshConnector getConnector() {
        return connector;
    }

    protected AdaptiveSshConfiguration getConfiguration() {
        return configuration;
    }

    protected Properties getProperties() {
        return properties;
    }

}