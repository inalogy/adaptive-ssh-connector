package com.inalogy.midpoint.connectors.utils.dynamicconfig;

public class CreateOperationSettings {
    private String alreadyExistsErrorParameter;
    private String successStatusMessage;

    public CreateOperationSettings(){}

    public String getAlreadyExistsErrorParameter() {
        return alreadyExistsErrorParameter;
    }

    public void setAlreadyExistsErrorParameter(String alreadyExistsErrorParameter) {
        this.alreadyExistsErrorParameter = alreadyExistsErrorParameter;
    }

    public String getSuccessStatusMessage() {
        return successStatusMessage;
    }

    public void setSuccessStatusMessage(String successStatusMessage) {
        this.successStatusMessage = successStatusMessage;
    }
}
