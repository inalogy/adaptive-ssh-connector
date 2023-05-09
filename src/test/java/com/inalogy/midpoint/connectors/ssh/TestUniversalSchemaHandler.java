package com.inalogy.midpoint.connectors.ssh;
import com.inalogy.midpoint.connectors.schema.UniversalSchemaHandler;
import org.testng.annotations.Test;

public class TestUniversalSchemaHandler {
    private TestProcessor testProcessor;

    private void init() {
        testProcessor = new TestProcessor();
    }

    @Test
    public void testUniversalSchemaHandler(){
        init();
        String schemaFilePath = testProcessor.getProperties().getProperty("testSchemaFilePath");
        String absoluteSchemaFilePath = System.getProperty("user.dir") + "/"+ schemaFilePath;
        
        UniversalSchemaHandler schemaHandler = new UniversalSchemaHandler(absoluteSchemaFilePath);
        System.out.println(schemaHandler.getName() + "\n" + schemaHandler.getScriptPath());
    }

}
