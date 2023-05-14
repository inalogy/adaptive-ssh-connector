package com.inalogy.midpoint.connectors.ssh;

import com.inalogy.midpoint.connectors.schema.SchemaType;
import com.inalogy.midpoint.connectors.schema.UniversalSchemaHandler;
import com.inalogy.midpoint.connectors.utils.SshResponseHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        String dummyResponse = readFileAsString("/Users/patrikrovnak/IdeaProjects/ssh-connector/src/test/resources/testingPowershellScripts/searchPSDummyResponse.txt");

        SchemaType schemaType = ush.getSchemaTypes().get("user");
        SshResponseHandler sshResponseHandler = new SshResponseHandler(schemaType, "searchScript", dummyResponse);
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
