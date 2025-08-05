package com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig;

public class ConnectorSettings {

    private FlagSettings icfsUidFlagEquivalent;
    private FlagSettings icfsNameFlagEquivalent;

    private FlagSettings replaceWhiteSpaceCharacterInAttributeValues;
    private FlagSettings addSudoExecution;
    private FlagSettings icfsPasswordFlagEquivalent;
    private FlagSettings preloadScript;

    private FlagSettings disposeScript;


    public ConnectorSettings(){}

    public FlagSettings getReplaceWhiteSpaceCharacterInAttributeValues() {
        return replaceWhiteSpaceCharacterInAttributeValues;
    }

    protected void setReplaceWhiteSpaceCharacterInAttributeValues(FlagSettings replaceWhiteSpaceCharacterInAttributeValues) {
        this.replaceWhiteSpaceCharacterInAttributeValues = replaceWhiteSpaceCharacterInAttributeValues;
    }

    public FlagSettings getAddSudoExecution() {
        return addSudoExecution;
    }

    protected void setAddSudoExecution(FlagSettings addSudoExecution) {
        this.addSudoExecution = addSudoExecution;
    }

    public FlagSettings getIcfsPasswordFlagEquivalent() {
        return icfsPasswordFlagEquivalent;
    }

    protected void setIcfsPasswordFlagEquivalent(FlagSettings icfsPasswordFlagEquivalent) {
        this.icfsPasswordFlagEquivalent = icfsPasswordFlagEquivalent;
    }

    public FlagSettings getIcfsUidFlagEquivalent() {
        return icfsUidFlagEquivalent;
    }

    protected void setIcfsUidFlagEquivalent(FlagSettings icfsUidFlagEquivalent) {
        this.icfsUidFlagEquivalent = icfsUidFlagEquivalent;
    }

    public FlagSettings getIcfsNameFlagEquivalent() {
        return icfsNameFlagEquivalent;
    }

    protected void setIcfsNameFlagEquivalent(FlagSettings icfsNameFlagEquivalent) {
        this.icfsNameFlagEquivalent = icfsNameFlagEquivalent;
    }


    public FlagSettings getDisposeScript() {
        return disposeScript;
    }

    public void setDisposeScript(FlagSettings disposeScript) {
        this.disposeScript = disposeScript;
    }
    public FlagSettings getPreloadScript() {
        return preloadScript;
    }

    protected void setPreloadScript(FlagSettings preloadScript) {
        this.preloadScript = preloadScript;
    }
}
