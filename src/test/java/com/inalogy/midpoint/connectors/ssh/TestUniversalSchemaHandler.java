package com.inalogy.midpoint.connectors.ssh;
import com.inalogy.midpoint.connectors.schema.SchemaType;
import com.inalogy.midpoint.connectors.schema.SchemaTypeAttribute;
import com.inalogy.midpoint.connectors.schema.UniversalSchemaHandler;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.Schema;
import org.testng.Assert;
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
        System.out.println(schemaTypeUser.getAttributes().get(0).getDataType());
    }
    @Test
    public void testSchema() {
        init();
        Schema schema = testProcessor.getConnector().schema();
        Assert.assertNotNull(schema);
        Assert.assertFalse(schema.getObjectClassInfo().isEmpty());

        for (ObjectClassInfo objectClassInfo : schema.getObjectClassInfo()) {
            Assert.assertFalse(objectClassInfo.getAttributeInfo().isEmpty());

            if (objectClassInfo.getType().equals("user")) {
                for (AttributeInfo attributeInfo : objectClassInfo.getAttributeInfo()) {
                    System.out.println(attributeInfo);
                }
            }
        }
    }

}
