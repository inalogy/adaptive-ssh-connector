package com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig;

public class DynamicConfigurationTestBuilder {

    public static DynamicConfiguration buildMinimal(String columnSep, String newLineSep,
                                                    String emptyAttr, String multiValuedSep) {
        DynamicConfiguration.resetInstance();
        DynamicConfiguration dc = DynamicConfiguration.getInstance();

        Settings s = new Settings();

        ScriptResponseSettings srs = new ScriptResponseSettings();
        srs.setResponseColumnSeparator(columnSep);
        srs.setResponseNewLineSeparator(newLineSep);
        srs.setScriptEmptyAttribute(emptyAttr);
        srs.setMultiValuedAttributeSeparator(multiValuedSep);
        s.setScriptResponseSettings(srs);

        ConnectorSettings cs = new ConnectorSettings();
        cs.setIcfsNameFlagEquivalent(flag(true, "name"));
        cs.setIcfsUidFlagEquivalent(flag(false, ""));
        cs.setIcfsPasswordFlagEquivalent(flag(false, "password"));
        cs.setReplaceWhiteSpaceCharacterInAttributeValues(flag(false, "---"));
        cs.setAddSudoExecution(flag(false, "sudo"));
        cs.setPreloadScript(flag(false, ""));
        cs.setDisposeScript(flag(false, ""));
        s.setConnectorSettings(cs);

        CreateOperationSettings cos = new CreateOperationSettings();
        cos.setAlreadyExistsErrorParameter("ObjectAlreadyExists");
        cos.setSuccessStatusMessage("");
        s.setCreateOperationSettings(cos);

        UpdateOperationSettings uos = new UpdateOperationSettings();
        uos.setUnknownUidException("UnknownUid");
        uos.setUpdateDeltaAddParameter("ADD:");
        uos.setUpdateDeltaRemoveParameter("REMOVE:");
        uos.setUpdateSuccessResponse("");
        s.setUpdateOperationSettings(uos);

        DeleteOperationSettings dos = new DeleteOperationSettings();
        dos.setDeleteSuccessResponse("");
        s.setDeleteOperationSettings(dos);

        SearchOperationSettings sos = new SearchOperationSettings();
        sos.setNoResultSuccessMessage("");
        sos.setGeneralFatalErrorMessage("__FATAL_ERROR__");
        s.setSearchOperationSettings(sos);

        dc.setSettings(s);
        dc.setConfigName("test");
        return dc;
    }

    public static DynamicConfiguration buildExchangeBasic() {
        return buildMinimal("||", "\n", "null", "~");
    }

    public static DynamicConfiguration buildExchangePersistent() {
        DynamicConfiguration dc = buildMinimal("|", "\n", "__NULL_VALUE__", " ");

        ConnectorSettings cs = dc.getSettings().getConnectorSettings();
        cs.setPreloadScript(flagWithSuccess(true, "/scripts/preload.ps1", "BASE-OK"));
        cs.setDisposeScript(flag(true, "/scripts/dispose.ps1"));

        dc.getSettings().getSearchOperationSettings().setGeneralFatalErrorMessage("__FATAL_ERROR__");
        return dc;
    }

    public static DynamicConfiguration buildOpenBsd() {
        DynamicConfiguration dc = buildMinimal("|", "\n", "null", "~");

        ConnectorSettings cs = dc.getSettings().getConnectorSettings();
        cs.setReplaceWhiteSpaceCharacterInAttributeValues(flag(true, "---"));
        cs.setIcfsPasswordFlagEquivalent(flag(true, "password"));
        cs.setAddSudoExecution(flag(false, "doas"));
        return dc;
    }

    public static DynamicConfiguration buildExchangeOnline() {
        DynamicConfiguration dc = buildMinimal("|", "\n", "__NULL_VALUE__", " ");
        dc.getSettings().getSearchOperationSettings().setNoResultSuccessMessage("__NO_RESULT__");
        return dc;
    }

    public static DynamicConfiguration buildWithSudo(String sudoValue) {
        DynamicConfiguration dc = buildExchangeBasic();
        dc.getSettings().getConnectorSettings().setAddSudoExecution(flag(true, sudoValue));
        return dc;
    }

    public static DynamicConfiguration buildWithUidFlag(String uidFlagValue) {
        DynamicConfiguration dc = buildExchangeBasic();
        dc.getSettings().getConnectorSettings().setIcfsUidFlagEquivalent(flag(true, uidFlagValue));
        return dc;
    }

    public static DynamicConfiguration buildWithNameFlagDisabled() {
        DynamicConfiguration dc = buildExchangeBasic();
        dc.getSettings().getConnectorSettings().setIcfsNameFlagEquivalent(flag(false, "name"));
        return dc;
    }

    public static void reset() {
        DynamicConfiguration.resetInstance();
    }

    private static FlagSettings flag(boolean enabled, String value) {
        FlagSettings fs = new FlagSettings();
        fs.setEnabled(enabled);
        fs.setValue(value);
        return fs;
    }

    private static FlagSettings flagWithSuccess(boolean enabled, String value, String successReturnValue) {
        FlagSettings fs = flag(enabled, value);
        fs.setSuccessReturnValue(successReturnValue);
        return fs;
    }
}
