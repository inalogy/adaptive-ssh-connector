package com.inalogy.midpoint.connector.ssh.utils.dynamicconfig;

public class FlagSettings {

    private boolean enabled;
    private String value;

    public FlagSettings(){}


    public boolean isEnabled() {
        return enabled;
    }

    protected void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getValue() {
        return value;
    }

    protected void setValue(String value) {
        this.value = value;
    }
}