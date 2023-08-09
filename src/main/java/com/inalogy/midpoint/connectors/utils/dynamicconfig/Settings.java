package com.inalogy.midpoint.connectors.utils.dynamicconfig;

public class Settings {
    private ScriptResponseSettings scriptResponseSettings;
    private ConnectorSettings connectorSettings;
    private CreateOperationSettings createOperationSettings;
    private UpdateOperationSettings updateOperationSettings;
    private DeleteOperationSettings deleteOperationSettings;
    private SearchOperationSettings searchOperationSettings;

    public ScriptResponseSettings getScriptResponseSettings() {
        return scriptResponseSettings;
    }

    public void setScriptResponseSettings(ScriptResponseSettings scriptResponseSettings) {
        this.scriptResponseSettings = scriptResponseSettings;
    }

    public ConnectorSettings getConnectorSettings() {
        return connectorSettings;
    }

    public void setConnectorSettings(ConnectorSettings connectorSettings) {
        this.connectorSettings = connectorSettings;
    }

    public CreateOperationSettings getCreateOperationSettings() {
        return createOperationSettings;
    }

    public void setCreateOperationSettings(CreateOperationSettings createOperationSettings) {
        this.createOperationSettings = createOperationSettings;
    }

    public UpdateOperationSettings getUpdateOperationSettings() {
        return updateOperationSettings;
    }

    public void setUpdateOperationSettings(UpdateOperationSettings updateOperationSettings) {
        this.updateOperationSettings = updateOperationSettings;
    }

    public DeleteOperationSettings getDeleteOperationSettings() {
        return deleteOperationSettings;
    }

    public void setDeleteOperationSettings(DeleteOperationSettings deleteOperationSettings) {
        this.deleteOperationSettings = deleteOperationSettings;
    }

    public SearchOperationSettings getSearchOperationSettings() {
        return searchOperationSettings;
    }

    public void setSearchOperationSettings(SearchOperationSettings searchOperationSettings) {
        this.searchOperationSettings = searchOperationSettings;
    }

    public Settings(){}

}