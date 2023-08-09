package com.inalogy.midpoint.connectors.utils.dynamicconfig;

public class SearchOperationSettings {
    private String noResultSuccessMessage;

    public SearchOperationSettings(){}


    public String getNoResultSuccessMessage() {
        return noResultSuccessMessage;
    }

    protected void setNoResultSuccessMessage(String noResultSuccessMessage) {
        this.noResultSuccessMessage = noResultSuccessMessage;
    }
}

