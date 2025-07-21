package com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig;

public class FlagSettings {

    private boolean enabled;
    private String value;
    private String successReturnValue;

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

    public String getSuccessReturnValue() {
        return successReturnValue;
    }

    public void setSuccessReturnValue(String successReturnValue) {
        this.successReturnValue = successReturnValue;
    }

    protected void setValue(String value) {
        this.value = value;
    }
}