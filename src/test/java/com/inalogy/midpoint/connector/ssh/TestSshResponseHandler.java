package com.inalogy.midpoint.connector.ssh;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestSshResponseHandler {

    TestProcessor testProcessor = new TestProcessor();
    private void init() {
        testProcessor = new TestProcessor();
    }
    //need rework
//    @Test
//    public void testSshResponseHandler(){
//
//        init();
//        String schemaFilePath = testProcessor.getConfiguration().getSchemaFilePath();
//        UniversalSchemaHandler ush = new UniversalSchemaHandler(schemaFilePath);
//        String searchPowerShellDummyResponse = System.getProperty("user.dir") + "/src/test/resources/testingPowershellScripts/searchPSDummyResponse.txt";
//        String dummyResponse = readFileAsString(searchPowerShellDummyResponse);
//        SchemaType schemaType = ush.getSchemaTypes().get("user");
//        ArrayList<ConnectorObject> sshResponseHandler = new SshResponseHandler(schemaType, dummyResponse).parseSearchOperation();
//        for (ConnectorObject connectorObject: sshResponseHandler){
//            System.out.println(connectorObject.getAttributes());
//        }
//        ObjectClass userClass = new ObjectClass("user");
//        testProcessor.getConnector().test();
//        testProcessor.getConnector().schema();
//        testProcessor.getConnector().executeQuery(userClass, null, null, null);
//    }
    public  String readFileAsString(String fileName) {
        try {
            return new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
