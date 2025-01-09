package com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig;

public class CreateOperationSettings {
    private String alreadyExistsErrorParameter;
    private String successStatusMessage;

    public CreateOperationSettings(){}

    public String getAlreadyExistsErrorParameter() {
        return alreadyExistsErrorParameter;
    }

    protected void setAlreadyExistsErrorParameter(String alreadyExistsErrorParameter) {
        this.alreadyExistsErrorParameter = alreadyExistsErrorParameter;
    }

    public String getSuccessStatusMessage() {
        return successStatusMessage;
    }

    protected void setSuccessStatusMessage(String successStatusMessage) {
        this.successStatusMessage = successStatusMessage;
    }
}
