package com.inalogy.midpoint.connector.ssh.utils.dynamicconfig;

public class ConnectorSettings {

    private FlagSettings replaceWhiteSpaceCharacterInAttributeValues;
    private FlagSettings addSudoExecution;
    private FlagSettings icfsPasswordFlagEquivalent;

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

    private FlagSettings icfsUidFlagEquivalent;
    private FlagSettings icfsNameFlagEquivalent;

}
