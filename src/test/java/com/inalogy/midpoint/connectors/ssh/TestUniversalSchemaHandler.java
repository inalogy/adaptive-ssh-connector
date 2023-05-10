package com.inalogy.midpoint.connectors.ssh;
import com.inalogy.midpoint.connectors.schema.SchemaType;
import com.inalogy.midpoint.connectors.schema.UniversalSchemaHandler;
import org.testng.annotations.Test;


public class TestUniversalSchemaHandler {
    private TestProcessor testProcessor;

    private void init() {
        testProcessor = new TestProcessor();
    }

    @Test
    public void testUniversalSchemaHandler() {
        init();
        String relativeSchemaFilePath = testProcessor.getConfiguration().getSchemaFilePath();
        String absoluteSchemaFilePath = System.getProperty("user.dir") + "/"+ relativeSchemaFilePath;
        UniversalSchemaHandler ush = new UniversalSchemaHandler(absoluteSchemaFilePath);
        assert (ush.getSchemaTypes().size() == 2);
        for (SchemaType schemaType: ush.getSchemaTypes()){
            if (schemaType.getName().equals("user")){
                assert schemaType.getScriptPath().equals("/home/somescript.ps2");
                assert (schemaType.getAttributes().size() == 3);
            }
        }
    }

}
