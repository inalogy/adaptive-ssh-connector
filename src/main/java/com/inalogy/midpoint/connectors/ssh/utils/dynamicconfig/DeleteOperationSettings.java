package com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig;

public class DeleteOperationSettings {
    private String deleteSuccessResponse;


    public DeleteOperationSettings(){}


    public String getDeleteSuccessResponse() {
        return deleteSuccessResponse;
    }

    protected void setDeleteSuccessResponse(String deleteSuccessResponse) {
        this.deleteSuccessResponse = deleteSuccessResponse;
    }
}

