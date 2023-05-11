package com.inalogy.midpoint.connectors.ssh;
import com.inalogy.midpoint.connectors.schema.SchemaType;
import com.inalogy.midpoint.connectors.schema.SchemaTypeAttribute;
import com.inalogy.midpoint.connectors.schema.UniversalSchemaHandler;
import org.testng.annotations.Test;

import javax.swing.plaf.synth.SynthCheckBoxMenuItemUI;
import java.util.HashMap;
import java.util.Map;


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
        Map<String, SchemaType> obj = ush.getSchemaTypes();
        SchemaType  schemaTypeUser = obj.get("someUniqueName");
        assert schemaTypeUser.getCreateScript().equals("createScript.ps2");
    }

}
