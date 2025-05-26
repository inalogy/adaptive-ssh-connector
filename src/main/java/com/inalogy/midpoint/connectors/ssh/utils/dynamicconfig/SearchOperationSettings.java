package com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig;

public class SearchOperationSettings {
    private String noResultSuccessMessage;

    private String generalFatalErrorMessage;

    public SearchOperationSettings(){}

    public String getGeneralFatalErrorMessage() {
        return generalFatalErrorMessage;
    }

    public void setGeneralFatalErrorMessage(String generalFatalErrorMessage) {
        this.generalFatalErrorMessage = generalFatalErrorMessage;
    }

    public String getNoResultSuccessMessage() {
        return noResultSuccessMessage;
    }

    protected void setNoResultSuccessMessage(String noResultSuccessMessage) {
        this.noResultSuccessMessage = noResultSuccessMessage;
    }
}

