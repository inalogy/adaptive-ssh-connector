package com.inalogy.midpoint.connectors.ssh.powershellschemavalidator;

import org.identityconnectors.framework.common.objects.Schema;
import org.testng.annotations.Test;

public class testPowerShellSchemaValidator {

    private PowerShellSchemaValidator powerShellSchemaValidator;
    private Schema ConnectorSchema;

    public void init(){
        powerShellSchemaValidator = new PowerShellSchemaValidator();
    }
    @Test
    public void testCreateScriptContainsReturnHeader(){
        init();
        String returnHeader= powerShellSchemaValidator.findReturnHeaderInScript(powerShellSchemaValidator.createScriptPath);
        System.out.println(returnHeader);
        assert (returnHeader.contains("returnHeader"));
    }
}
