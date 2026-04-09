package com.inalogy.midpoint.connectors.ssh.unit;

import com.inalogy.midpoint.connectors.ssh.objects.UniversalObjectsHandler;
import com.inalogy.midpoint.connectors.ssh.schema.SchemaType;
import com.inalogy.midpoint.connectors.ssh.schema.SchemaTypeTestFactory;
import com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig.DynamicConfiguration;
import com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig.DynamicConfigurationTestBuilder;

import org.identityconnectors.framework.common.objects.*;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.*;

@Test(groups = "unit")
public class UniversalObjectsHandlerTest {

    @AfterMethod
    public void tearDown() {
        DynamicConfigurationTestBuilder.reset();
    }

    @Test
    public void testConvert_basic() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        SchemaType schema = SchemaTypeTestFactory.exchangeUserSchema();
        Map<String, String> attrs = new LinkedHashMap<>();
        attrs.put("icfsUid", "guid-1");
        attrs.put("icfsName", "user1@test.com");
        attrs.put("email", "user1@mail.com");

        ConnectorObject obj = UniversalObjectsHandler.convertObjectToConnectorObject(schema, attrs, dc);

        Assert.assertEquals(obj.getUid().getUidValue(), "guid-1");
        Assert.assertEquals(obj.getName().getNameValue(), "user1@test.com");
        Assert.assertEquals(AttributeUtil.getStringValue(obj.getAttributeByName("email")), "user1@mail.com");
    }

    @Test
    public void testConvert_multivaluedAttribute_tildeSeparator() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic(); // mvSep = "~"
        SchemaType schema = SchemaTypeTestFactory.exchangeUserSchema();
        Map<String, String> attrs = new LinkedHashMap<>();
        attrs.put("icfsUid", "guid-1");
        attrs.put("icfsName", "user1@test.com");
        attrs.put("emailAddresses", "addr1@test.com~addr2@test.com~addr3@test.com");

        ConnectorObject obj = UniversalObjectsHandler.convertObjectToConnectorObject(schema, attrs, dc);

        Attribute emailAddrs = obj.getAttributeByName("emailAddresses");
        Assert.assertNotNull(emailAddrs);
        Assert.assertEquals(emailAddrs.getValue().size(), 3);
        Assert.assertTrue(emailAddrs.getValue().contains("addr1@test.com"));
        Assert.assertTrue(emailAddrs.getValue().contains("addr2@test.com"));
        Assert.assertTrue(emailAddrs.getValue().contains("addr3@test.com"));
    }

    @Test
    public void testConvert_multivaluedAttribute_spaceSeparator() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangePersistent(); // mvSep = " " (space)
        SchemaType schema = SchemaTypeTestFactory.exchangeUserSchema();
        Map<String, String> attrs = new LinkedHashMap<>();
        attrs.put("icfsUid", "guid-1");
        attrs.put("icfsName", "user1@test.com");
        attrs.put("emailAddresses", "addr1@test.com addr2@test.com");

        ConnectorObject obj = UniversalObjectsHandler.convertObjectToConnectorObject(schema, attrs, dc);

        Attribute emailAddrs = obj.getAttributeByName("emailAddresses");
        Assert.assertNotNull(emailAddrs);
        Assert.assertEquals(emailAddrs.getValue().size(), 2);
    }

    @Test
    public void testConvert_nullMultivaluedAttribute_notAdded() {
        // Regression: v1.2.1 bug — null/empty multi-valued should not be added
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        SchemaType schema = SchemaTypeTestFactory.exchangeUserSchema();
        Map<String, String> attrs = new LinkedHashMap<>();
        attrs.put("icfsUid", "guid-1");
        attrs.put("icfsName", "user1@test.com");
        attrs.put("emailAddresses", null);

        ConnectorObject obj = UniversalObjectsHandler.convertObjectToConnectorObject(schema, attrs, dc);

        Attribute emailAddrs = obj.getAttributeByName("emailAddresses");
        Assert.assertNull(emailAddrs);
    }

    @Test
    public void testConvert_emptyMultivaluedAttribute_notAdded() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        SchemaType schema = SchemaTypeTestFactory.exchangeUserSchema();
        Map<String, String> attrs = new LinkedHashMap<>();
        attrs.put("icfsUid", "guid-1");
        attrs.put("icfsName", "user1@test.com");
        attrs.put("emailAddresses", "");

        ConnectorObject obj = UniversalObjectsHandler.convertObjectToConnectorObject(schema, attrs, dc);

        Attribute emailAddrs = obj.getAttributeByName("emailAddresses");
        Assert.assertNull(emailAddrs);
    }

    @Test
    public void testConvert_singleValuedNull() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        SchemaType schema = SchemaTypeTestFactory.exchangeUserSchema();
        Map<String, String> attrs = new LinkedHashMap<>();
        attrs.put("icfsUid", "guid-1");
        attrs.put("icfsName", "user1@test.com");
        attrs.put("email", null);

        ConnectorObject obj = UniversalObjectsHandler.convertObjectToConnectorObject(schema, attrs, dc);

        Attribute email = obj.getAttributeByName("email");
        Assert.assertNotNull(email);
        // Single-valued null attribute should have null value in the list
        Assert.assertEquals(email.getValue(), Collections.singletonList(null));
    }

    @Test
    public void testBuildObjectClass_allAttributeMetadata() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        SchemaType schema = SchemaTypeTestFactory.exchangeUserSchema();
        SchemaBuilder schemaBuilder = new SchemaBuilder(com.inalogy.midpoint.connectors.ssh.AdaptiveSshConnector.class);

        UniversalObjectsHandler.buildObjectClass(schemaBuilder, schema, dc);

        Schema built = schemaBuilder.build();
        Assert.assertFalse(built.getObjectClassInfo().isEmpty());
        ObjectClassInfo oci = built.getObjectClassInfo().iterator().next();
        Assert.assertEquals(oci.getType(), "user");

        // Check email attribute metadata
        AttributeInfo emailInfo = null;
        for (AttributeInfo ai : oci.getAttributeInfo()) {
            if ("email".equals(ai.getName())) {
                emailInfo = ai;
                break;
            }
        }
        Assert.assertNotNull(emailInfo);
        Assert.assertFalse(emailInfo.isRequired());
        Assert.assertTrue(emailInfo.isCreateable());
        Assert.assertTrue(emailInfo.isUpdateable());
        Assert.assertFalse(emailInfo.isMultiValued());
        Assert.assertTrue(emailInfo.isReturnedByDefault());
        Assert.assertTrue(emailInfo.isReadable());
    }

    @Test
    public void testBuildObjectClass_withPasswordAttribute() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildOpenBsd(); // password flag enabled
        SchemaType schema = SchemaTypeTestFactory.openBsdUserSchema();
        // Add a "password" attribute to match the icfsPasswordFlagEquivalent
        List<com.inalogy.midpoint.connectors.ssh.schema.SchemaTypeAttribute> attrs = new ArrayList<>(schema.getAttributes());
        attrs.add(SchemaTypeTestFactory.attr("password", true, true, false, false, "String", false, false));
        SchemaType schemaWithPassword = SchemaTypeTestFactory.create(
                schema.getIcfsUid(), schema.getIcfsName(), schema.getObjectClassName(),
                schema.getCreateScript(), schema.getUpdateScript(),
                schema.getDeleteScript(), schema.getSearchScript(), attrs
        );

        SchemaBuilder schemaBuilder = new SchemaBuilder(com.inalogy.midpoint.connectors.ssh.AdaptiveSshConnector.class);
        UniversalObjectsHandler.buildObjectClass(schemaBuilder, schemaWithPassword, dc);

        Schema built = schemaBuilder.build();
        ObjectClassInfo oci = built.getObjectClassInfo().iterator().next();

        // Should have __PASSWORD__ operational attribute instead of "password" regular attribute
        boolean hasPasswordOp = false;
        boolean hasPasswordRegular = false;
        for (AttributeInfo ai : oci.getAttributeInfo()) {
            if (OperationalAttributeInfos.PASSWORD.equals(ai)) {
                hasPasswordOp = true;
            }
            if ("password".equals(ai.getName())) {
                hasPasswordRegular = true;
            }
        }
        Assert.assertTrue(hasPasswordOp, "Should have operational __PASSWORD__ attribute");
        Assert.assertFalse(hasPasswordRegular, "Should NOT have regular 'password' attribute");
    }

    @Test
    public void testFormatMultiValued_addAndRemove() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        AttributeDelta delta = AttributeDeltaBuilder.build(
                "emailAddresses",
                Arrays.asList("new@test.com"),
                Arrays.asList("old@test.com")
        );

        Set<Attribute> result = UniversalObjectsHandler.formatMultiValuedAttribute(delta, dc);

        Assert.assertEquals(result.size(), 1);
        Attribute attr = result.iterator().next();
        Assert.assertEquals(attr.getName(), "emailAddresses");
        List<Object> values = attr.getValue();
        Assert.assertTrue(values.contains("ADD:new@test.com"));
        Assert.assertTrue(values.contains("REMOVE:old@test.com"));
    }

    @Test
    public void testFormatMultiValued_addOnly() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        AttributeDelta delta = AttributeDeltaBuilder.build(
                "emailAddresses",
                Arrays.asList("new1@test.com", "new2@test.com"),
                null
        );

        Set<Attribute> result = UniversalObjectsHandler.formatMultiValuedAttribute(delta, dc);
        Attribute attr = result.iterator().next();
        List<Object> values = attr.getValue();
        Assert.assertEquals(values.size(), 2);
        Assert.assertTrue(values.contains("ADD:new1@test.com"));
        Assert.assertTrue(values.contains("ADD:new2@test.com"));
    }

    @Test
    public void testFormatMultiValued_removeOnly() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        AttributeDelta delta = AttributeDeltaBuilder.build(
                "emailAddresses",
                null,
                Arrays.asList("old@test.com")
        );

        Set<Attribute> result = UniversalObjectsHandler.formatMultiValuedAttribute(delta, dc);
        Attribute attr = result.iterator().next();
        List<Object> values = attr.getValue();
        Assert.assertEquals(values.size(), 1);
        Assert.assertTrue(values.contains("REMOVE:old@test.com"));
    }
}
