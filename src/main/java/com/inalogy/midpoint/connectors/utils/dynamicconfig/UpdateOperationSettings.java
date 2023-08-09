package com.inalogy.midpoint.connectors.utils.dynamicconfig;

public class UpdateOperationSettings {
    private String updateDeltaAddParameter;
    private String updateDeltaRemoveParameter;
    private String updateSuccessResponse;

    private String unknownUidException;


    public UpdateOperationSettings(){}
    public String getUnknownUidException() {
        return unknownUidException;
    }

    public void setUnknownUidException(String unknownUidException) {
        this.unknownUidException = unknownUidException;
    }
    public String getUpdateDeltaAddParameter() {
        return updateDeltaAddParameter;
    }

    public void setUpdateDeltaAddParameter(String updateDeltaAddParameter) {
        this.updateDeltaAddParameter = updateDeltaAddParameter;
    }

    public String getUpdateDeltaRemoveParameter() {
        return updateDeltaRemoveParameter;
    }

    public void setUpdateDeltaRemoveParameter(String updateDeltaRemoveParameter) {
        this.updateDeltaRemoveParameter = updateDeltaRemoveParameter;
    }

    public String getUpdateSuccessResponse() {
        return updateSuccessResponse;
    }

    public void setUpdateSuccessResponse(String updateSuccessResponse) {
        this.updateSuccessResponse = updateSuccessResponse;
    }
}
