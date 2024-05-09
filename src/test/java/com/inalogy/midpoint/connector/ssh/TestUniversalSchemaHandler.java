package com.inalogy.midpoint.connector.ssh;
import com.inalogy.midpoint.connector.ssh.schema.SchemaType;
import com.inalogy.midpoint.connector.ssh.schema.UniversalSchemaHandler;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.Schema;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;


public class TestUniversalSchemaHandler {
    private TestProcessor testProcessor;

    private void init() {
        testProcessor = new TestProcessor();
    }

    @Test
    public void testUniversalSchemaHandler() {
        init();
        String schemaFilePath = testProcessor.getConfiguration().getSchemaFilePath();
//        String absoluteSchemaFilePath = System.getProperty("user.dir") + "/"+ relativeSchemaFilePath;
        UniversalSchemaHandler ush = new UniversalSchemaHandler(schemaFilePath);
        Map<String, SchemaType> obj = ush.getSchemaTypes();
        SchemaType  schemaTypeUser = obj.get("user");
//        assert schemaTypeUser.getCreateScript().equals("createScript.ps2");
        System.out.println(schemaTypeUser.getAttributes().get(0).getDataType());
    }
    @Test
    public void testSchema() {

        init();
        Schema schema = testProcessor.getConnector().schema();
        Assert.assertNotNull(schema);
        Assert.assertFalse(schema.getObjectClassInfo().isEmpty());

        System.out.println("Schema: " + schema);

        for (ObjectClassInfo objectClassInfo : schema.getObjectClassInfo()) {
            Assert.assertFalse(objectClassInfo.getAttributeInfo().isEmpty());

            if (objectClassInfo.getType().equals("user")) {
                for (AttributeInfo attributeInfo : objectClassInfo.getAttributeInfo()) {
                    System.out.println(attributeInfo);
                }
            }
        }
    }

@Test
    public void testTest(){
        init();
        SshConnector connector = testProcessor.getConnector();

        connector.test();
}

}
