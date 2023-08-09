package com.inalogy.midpoint.connectors.utils.dynamicconfig;

public class ConnectorSettings {

    private FlagSettings replaceWhiteSpaceCharacterInAttributeValues;
    private FlagSettings addSudoExecution;
    private FlagSettings icfsPasswordFlagEquivalent;

    public ConnectorSettings(){}

    public FlagSettings getReplaceWhiteSpaceCharacterInAttributeValues() {
        return replaceWhiteSpaceCharacterInAttributeValues;
    }

    public void setReplaceWhiteSpaceCharacterInAttributeValues(FlagSettings replaceWhiteSpaceCharacterInAttributeValues) {
        this.replaceWhiteSpaceCharacterInAttributeValues = replaceWhiteSpaceCharacterInAttributeValues;
    }

    public FlagSettings getAddSudoExecution() {
        return addSudoExecution;
    }

    public void setAddSudoExecution(FlagSettings addSudoExecution) {
        this.addSudoExecution = addSudoExecution;
    }

    public FlagSettings getIcfsPasswordFlagEquivalent() {
        return icfsPasswordFlagEquivalent;
    }

    public void setIcfsPasswordFlagEquivalent(FlagSettings icfsPasswordFlagEquivalent) {
        this.icfsPasswordFlagEquivalent = icfsPasswordFlagEquivalent;
    }

    public FlagSettings getIcfsUidFlagEquivalent() {
        return icfsUidFlagEquivalent;
    }

    public void setIcfsUidFlagEquivalent(FlagSettings icfsUidFlagEquivalent) {
        this.icfsUidFlagEquivalent = icfsUidFlagEquivalent;
    }

    public FlagSettings getIcfsNameFlagEquivalent() {
        return icfsNameFlagEquivalent;
    }

    public void setIcfsNameFlagEquivalent(FlagSettings icfsNameFlagEquivalent) {
        this.icfsNameFlagEquivalent = icfsNameFlagEquivalent;
    }

    private FlagSettings icfsUidFlagEquivalent;
    private FlagSettings icfsNameFlagEquivalent;

}
