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

    protected void setScriptResponseSettings(ScriptResponseSettings scriptResponseSettings) {
        this.scriptResponseSettings = scriptResponseSettings;
    }

    public ConnectorSettings getConnectorSettings() {
        return connectorSettings;
    }

    protected void setConnectorSettings(ConnectorSettings connectorSettings) {
        this.connectorSettings = connectorSettings;
    }

    public CreateOperationSettings getCreateOperationSettings() {
        return createOperationSettings;
    }

    protected void setCreateOperationSettings(CreateOperationSettings createOperationSettings) {
        this.createOperationSettings = createOperationSettings;
    }

    public UpdateOperationSettings getUpdateOperationSettings() {
        return updateOperationSettings;
    }

    protected void setUpdateOperationSettings(UpdateOperationSettings updateOperationSettings) {
        this.updateOperationSettings = updateOperationSettings;
    }

    public DeleteOperationSettings getDeleteOperationSettings() {
        return deleteOperationSettings;
    }

    protected void setDeleteOperationSettings(DeleteOperationSettings deleteOperationSettings) {
        this.deleteOperationSettings = deleteOperationSettings;
    }

    public SearchOperationSettings getSearchOperationSettings() {
        return searchOperationSettings;
    }

    protected void setSearchOperationSettings(SearchOperationSettings searchOperationSettings) {
        this.searchOperationSettings = searchOperationSettings;
    }

    public Settings(){}

}