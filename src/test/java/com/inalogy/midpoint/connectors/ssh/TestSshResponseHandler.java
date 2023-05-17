package com.inalogy.midpoint.connectors.ssh;

import com.inalogy.midpoint.connectors.schema.SchemaType;
import com.inalogy.midpoint.connectors.schema.UniversalSchemaHandler;
import com.inalogy.midpoint.connectors.utils.Constants;
import com.inalogy.midpoint.connectors.utils.SshResponseHandler;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class TestSshResponseHandler {

    TestProcessor testProcessor = new TestProcessor();
    private void init() {
        testProcessor = new TestProcessor();
    }
    @Test
    public void testSshResponseHandler(){

        init();
        String schemaFilePath = testProcessor.getConfiguration().getSchemaFilePath();
        UniversalSchemaHandler ush = new UniversalSchemaHandler(schemaFilePath);
        String searchPowerShellDummyResponse = System.getProperty("user.dir") + "/src/test/resources/testingPowershellScripts/searchPSDummyResponse.txt";
        String dummyResponse = readFileAsString(searchPowerShellDummyResponse);
        SchemaType schemaType = ush.getSchemaTypes().get("user");
        ArrayList<ConnectorObject> sshResponseHandler = new SshResponseHandler(schemaType, Constants.SEARCH_OPERATION, dummyResponse).parseResponse();
        ObjectClass userClass = new ObjectClass("user");
        testProcessor.getConnector().test();
        testProcessor.getConnector().schema();
        testProcessor.getConnector().executeQuery(userClass, null, null, null);
    }
    public  String readFileAsString(String fileName) {
        try {
            return new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
