package com.inalogy.midpoint.connector.ssh.utils.dynamicconfig;

import org.identityconnectors.common.logging.Log;

public class ConnectorSettings {

    private FlagSettings replaceWhiteSpaceCharacterInAttributeValues;
    private FlagSettings addSudoExecution;
    private FlagSettings icfsPasswordFlagEquivalent;
    private static final Log LOG = Log.getLog(ConnectorSettings.class);

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

    /**
     * @deprecated This method has been removed and should not be used. executeQuery gets icfsUid from schemaType!
     */
    public FlagSettings getIcfsUidFlagEquivalent() {
        return icfsUidFlagEquivalent;
    }

    /**
     * @deprecated This method has been removed and should not be used. executeQuery gets icfsUid from schemaType!
     */
    @Deprecated
    protected void setIcfsUidFlagEquivalent(FlagSettings icfsUidFlagEquivalent) {
        LOG.warn("Detected old version of connectorConfig 'icfsUidFlagEquivalent' was removed. Please get new copy sample of connectorConfig from version control");
        this.icfsUidFlagEquivalent = icfsUidFlagEquivalent;
    }

    /**
     * @deprecated This method has been removed and should not be used. executeQuery gets icfsName from schemaType!
     */
    @Deprecated
    public FlagSettings getIcfsNameFlagEquivalent() {
        return icfsNameFlagEquivalent;
    }

    /**
     * @deprecated This method has been removed and should not be used. ExecuteQuery gets icfsName from schemaType!
     */
    @Deprecated
    protected void setIcfsNameFlagEquivalent(FlagSettings icfsNameFlagEquivalent) {
        LOG.warn("Detected old version of connectorConfig 'icfsNameFlagEquivalent' was removed. Please get new copy sample of connectorConfig from version control.");
        this.icfsNameFlagEquivalent = icfsNameFlagEquivalent;
    }

    private FlagSettings icfsUidFlagEquivalent;
    private FlagSettings icfsNameFlagEquivalent;

}
