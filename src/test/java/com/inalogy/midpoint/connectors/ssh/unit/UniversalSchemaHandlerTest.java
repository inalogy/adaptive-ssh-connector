package com.inalogy.midpoint.connectors.ssh.unit;

import com.inalogy.midpoint.connectors.ssh.schema.SchemaType;
import com.inalogy.midpoint.connectors.ssh.schema.SchemaTypeAttribute;
import com.inalogy.midpoint.connectors.ssh.schema.UniversalSchemaHandler;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.Map;

@Test(groups = "unit")
public class UniversalSchemaHandlerTest {

    private String resourcePath(String filename) {
        return Paths.get("src/test/resources/unit", filename).toAbsolutePath().toString();
    }

    @Test
    public void testLoadSchema_basic_twoObjectClasses() {
        UniversalSchemaHandler handler = new UniversalSchemaHandler(resourcePath("schema-basic.json"));
        Map<String, SchemaType> types = handler.getSchemaTypes();

        Assert.assertEquals(types.size(), 2);
        Assert.assertTrue(types.containsKey("user"));
        Assert.assertTrue(types.containsKey("group"));
    }

    @Test
    public void testLoadSchema_userObjectClass_correctScripts() {
        UniversalSchemaHandler handler = new UniversalSchemaHandler(resourcePath("schema-basic.json"));
        SchemaType user = handler.getSchemaTypes().get("user");

        Assert.assertEquals(user.getCreateScript(), "/scripts/usr/createScript.ps1");
        Assert.assertEquals(user.getUpdateScript(), "/scripts/usr/updateScript.ps1");
        Assert.assertEquals(user.getDeleteScript(), "/scripts/usr/deleteScript.ps1");
        Assert.assertEquals(user.getSearchScript(), "/scripts/usr/searchScript.ps1");
    }

    @Test
    public void testLoadSchema_userAttributes_correctProperties() {
        UniversalSchemaHandler handler = new UniversalSchemaHandler(resourcePath("schema-basic.json"));
        SchemaType user = handler.getSchemaTypes().get("user");

        Assert.assertEquals(user.getAttributes().size(), 3); // email, emailAddresses, enabled

        SchemaTypeAttribute emailAttr = user.getAttributes().stream()
                .filter(a -> a.getAttributeName().equals("email"))
                .findFirst().orElse(null);
        Assert.assertNotNull(emailAttr);
        Assert.assertFalse(emailAttr.isRequired());
        Assert.assertTrue(emailAttr.isCreatable());
        Assert.assertTrue(emailAttr.isUpdateable());
        Assert.assertFalse(emailAttr.isMultivalued());
        Assert.assertEquals(emailAttr.getDataType(), String.class);
        Assert.assertTrue(emailAttr.isReturnedByDefault());
        Assert.assertTrue(emailAttr.isReadable());

        SchemaTypeAttribute emailAddresses = user.getAttributes().stream()
                .filter(a -> a.getAttributeName().equals("emailAddresses"))
                .findFirst().orElse(null);
        Assert.assertNotNull(emailAddresses);
        Assert.assertTrue(emailAddresses.isMultivalued());
    }

    @Test
    public void testLoadSchema_booleanDataType() {
        UniversalSchemaHandler handler = new UniversalSchemaHandler(resourcePath("schema-basic.json"));
        SchemaType user = handler.getSchemaTypes().get("user");

        SchemaTypeAttribute enabled = user.getAttributes().stream()
                .filter(a -> a.getAttributeName().equals("enabled"))
                .findFirst().orElse(null);
        Assert.assertNotNull(enabled);
        Assert.assertEquals(enabled.getDataType(), Boolean.class);
        Assert.assertFalse(enabled.isCreatable());
    }

    @Test
    public void testLoadSchema_uidAndNameSame() {
        UniversalSchemaHandler handler = new UniversalSchemaHandler(resourcePath("schema-uid-equals-name.json"));
        SchemaType user = handler.getSchemaTypes().get("user");

        Assert.assertEquals(user.getIcfsUid(), "uid");
        Assert.assertEquals(user.getIcfsName(), "uid");
        Assert.assertTrue(user.isUidAndNameSame());
    }

    @Test
    public void testLoadSchema_uidAndNameDifferent() {
        UniversalSchemaHandler handler = new UniversalSchemaHandler(resourcePath("schema-basic.json"));
        SchemaType user = handler.getSchemaTypes().get("user");

        Assert.assertEquals(user.getIcfsUid(), "ExchangeGuid");
        Assert.assertEquals(user.getIcfsName(), "UserPrincipalName");
        Assert.assertFalse(user.isUidAndNameSame());
    }

    @Test
    public void testLoadSchema_noAttributes() {
        UniversalSchemaHandler handler = new UniversalSchemaHandler(resourcePath("schema-no-attributes.json"));
        SchemaType account = handler.getSchemaTypes().get("account");

        Assert.assertNotNull(account);
        Assert.assertNotNull(account.getAttributes());
        Assert.assertTrue(account.getAttributes().isEmpty());
    }

    @Test
    public void testLoadSchema_multipleObjectClasses() {
        UniversalSchemaHandler handler = new UniversalSchemaHandler(resourcePath("schema-multiple-objectclasses.json"));
        Map<String, SchemaType> types = handler.getSchemaTypes();

        Assert.assertEquals(types.size(), 4);
        Assert.assertTrue(types.containsKey("MailboxOnPrem"));
        Assert.assertTrue(types.containsKey("MailboxCloud"));
        Assert.assertTrue(types.containsKey("MailUser"));
        Assert.assertTrue(types.containsKey("MailContact"));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testLoadSchema_nonExistentFile_throws() {
        new UniversalSchemaHandler("/nonexistent/path/schema.json");
    }

    @Test
    public void testGetFileSha256_deterministic() {
        String path = resourcePath("schema-basic.json");
        UniversalSchemaHandler handler1 = new UniversalSchemaHandler(path);
        UniversalSchemaHandler handler2 = new UniversalSchemaHandler(path);

        Assert.assertNotNull(handler1.getFileSha256());
        Assert.assertEquals(handler1.getFileSha256(), handler2.getFileSha256());
    }

    @Test
    public void testLoadSchema_groupAttributes() {
        UniversalSchemaHandler handler = new UniversalSchemaHandler(resourcePath("schema-basic.json"));
        SchemaType group = handler.getSchemaTypes().get("group");

        Assert.assertEquals(group.getAttributes().size(), 2);

        SchemaTypeAttribute members = group.getAttributes().stream()
                .filter(a -> a.getAttributeName().equals("members"))
                .findFirst().orElse(null);
        Assert.assertNotNull(members);
        Assert.assertTrue(members.isMultivalued());
        Assert.assertFalse(members.isCreatable());
        Assert.assertTrue(members.isUpdateable());
    }
}
