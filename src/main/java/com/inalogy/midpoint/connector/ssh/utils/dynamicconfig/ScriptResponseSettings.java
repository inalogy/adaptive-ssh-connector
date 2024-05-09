package com.inalogy.midpoint.connector.ssh.utils.dynamicconfig;

public class ScriptResponseSettings {

    private String scriptEmptyAttribute;


    public ScriptResponseSettings(){}

    public String getScriptEmptyAttribute() {
        return scriptEmptyAttribute;
    }

    protected void setScriptEmptyAttribute(String scriptEmptyAttribute) {
        this.scriptEmptyAttribute = scriptEmptyAttribute;
    }

    public String getMultiValuedAttributeSeparator() {
        return multiValuedAttributeSeparator;
    }

    protected void setMultiValuedAttributeSeparator(String multiValuedAttributeSeparator) {
        this.multiValuedAttributeSeparator = multiValuedAttributeSeparator;
    }

    public String getResponseNewLineSeparator() {
        return responseNewLineSeparator;
    }

    protected void setResponseNewLineSeparator(String responseNewLineSeparator) {
        this.responseNewLineSeparator = responseNewLineSeparator;
    }

    public String getResponseColumnSeparator() {
        return responseColumnSeparator;
    }

    protected void setResponseColumnSeparator(String responseColumnSeparator) {
        this.responseColumnSeparator = responseColumnSeparator;
    }

    private String multiValuedAttributeSeparator;
    private String responseNewLineSeparator;
    private String responseColumnSeparator;

}
