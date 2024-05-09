package com.inalogy.midpoint.connector.ssh.utils.dynamicconfig;

public class SearchOperationSettings {
    private String noResultSuccessMessage;
    private boolean ignoreInvalidResultObjects;

    public SearchOperationSettings(){}

    public boolean getIgnoreInvalidResultObjects() {
        return ignoreInvalidResultObjects;
    }
    public String getNoResultSuccessMessage() {
        return noResultSuccessMessage;
    }

    protected void setNoResultSuccessMessage(String noResultSuccessMessage) {
        this.noResultSuccessMessage = noResultSuccessMessage;
    }
    protected void setIgnoreInvalidResultObjects(boolean ignoreInvalidResultObjects) {
        this.ignoreInvalidResultObjects = ignoreInvalidResultObjects;
    }
}

