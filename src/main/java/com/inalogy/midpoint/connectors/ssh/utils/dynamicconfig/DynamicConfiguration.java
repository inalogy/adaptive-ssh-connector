package com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig;

import org.identityconnectors.common.logging.Log;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class DynamicConfiguration {

    private static final Log LOG = Log.getLog(DynamicConfiguration.class);
    // Static instance of the class
    private static DynamicConfiguration instance;
    private boolean isInitialized = false;
    @JsonProperty("configName")
    private String configName;
    @JsonProperty("settings")
    private Settings settings;

    // Default constructor for Jackson
    private DynamicConfiguration() {
    }


    public static DynamicConfiguration getInstance() {
        if (instance == null) {
            instance = new DynamicConfiguration();
        }
        return instance;
    }
    // Initialization method
    public void init(String externalConfigurationPath) {
        LOG.info("initializing dynamic configuration");
        if (externalConfigurationPath == null) {
            return;  // Do nothing if path is null
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            DynamicConfiguration config = objectMapper.readValue(new File(externalConfigurationPath), DynamicConfiguration.class);
            this.settings = config.getSettings();
            this.configName = config.getConfigName();
            isInitialized = true;  // Set the flag
            LOG.info("{0} configuration successfully initialized", this.configName);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public String getScriptEmptyAttribute() {
        return settings.getScriptResponseSettings().getScriptEmptyAttribute();
    }

    public boolean isReplaceWhiteSpaceEnabled() {
        return settings.getConnectorSettings().getReplaceWhiteSpaceCharacterInAttributeValues().isEnabled();
    }

    public String getReplaceWhiteSpaceValue() {
        return settings.getConnectorSettings().getReplaceWhiteSpaceCharacterInAttributeValues().getValue();
    }

    public Settings getSettings() {
        return settings;
    }

    protected void setSettings(Settings settings) {
        this.settings = settings;
    }

    public String getConfigName() {
        return configName;
    }

    protected void setConfigName(String configName) {
        this.configName = configName;
    }
}
