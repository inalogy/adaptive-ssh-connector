package com.inalogy.midpoint.connectors.utils.dynamicconfig;

public class ScriptResponseSettings {

    private String scriptEmptyAttribute;


    public ScriptResponseSettings(){}

    public String getScriptEmptyAttribute() {
        return scriptEmptyAttribute;
    }

    public void setScriptEmptyAttribute(String scriptEmptyAttribute) {
        this.scriptEmptyAttribute = scriptEmptyAttribute;
    }

    public String getMultiValuedAttributeSeparator() {
        return multiValuedAttributeSeparator;
    }

    public void setMultiValuedAttributeSeparator(String multiValuedAttributeSeparator) {
        this.multiValuedAttributeSeparator = multiValuedAttributeSeparator;
    }

    public String getResponseNewLineSeparator() {
        return responseNewLineSeparator;
    }

    public void setResponseNewLineSeparator(String responseNewLineSeparator) {
        this.responseNewLineSeparator = responseNewLineSeparator;
    }

    public String getResponseColumnSeparator() {
        return responseColumnSeparator;
    }

    public void setResponseColumnSeparator(String responseColumnSeparator) {
        this.responseColumnSeparator = responseColumnSeparator;
    }

    private String multiValuedAttributeSeparator;
    private String responseNewLineSeparator;
    private String responseColumnSeparator;

}
