package com.inalogy.midpoint.connectors.ssh.unit;

import com.inalogy.midpoint.connectors.ssh.exceptions.InvalidCreateScriptOutputException;
import com.inalogy.midpoint.connectors.ssh.exceptions.NoCreateScriptResponseException;
import com.inalogy.midpoint.connectors.ssh.schema.SchemaType;
import com.inalogy.midpoint.connectors.ssh.schema.SchemaTypeTestFactory;
import com.inalogy.midpoint.connectors.ssh.utils.SshResponseHandler;
import com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig.DynamicConfiguration;
import com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig.DynamicConfigurationTestBuilder;

import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.Uid;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Set;

@Test(groups = "unit")
public class SshResponseHandlerTest {

    private SchemaType exchangeSchema;
    private SchemaType openBsdSchema;
    private DynamicConfiguration dcExchange;

    @BeforeMethod
    public void setUp() {
        exchangeSchema = SchemaTypeTestFactory.exchangeUserSchema();
        openBsdSchema = SchemaTypeTestFactory.openBsdUserSchema();
    }

    @AfterMethod
    public void tearDown() {
        DynamicConfigurationTestBuilder.reset();
    }

    private DynamicConfiguration exchangeBasic() {
        return DynamicConfigurationTestBuilder.buildExchangeBasic();
    }

    private DynamicConfiguration exchangePersistent() {
        return DynamicConfigurationTestBuilder.buildExchangePersistent();
    }

    @Test
    public void testParseSearch_singleRow_doublePipeSeparator() {
        DynamicConfiguration dc = exchangeBasic();
        String response = "ExchangeGuid||UserPrincipalName||email\nguid-1||user1@test.com||user1@mail.com";
        SshResponseHandler handler = new SshResponseHandler(exchangeSchema, response, dc);

        Set<Map<String, String>> result = handler.parseSearchOperation();

        Assert.assertEquals(result.size(), 1);
        Map<String, String> row = result.iterator().next();
        Assert.assertEquals(row.get("icfsUid"), "guid-1");
        Assert.assertEquals(row.get("icfsName"), "user1@test.com");
        Assert.assertEquals(row.get("email"), "user1@mail.com");
    }

    @Test
    public void testParseSearch_multipleRows() {
        DynamicConfiguration dc = exchangeBasic();
        String response = "ExchangeGuid||UserPrincipalName||email\n" +
                "guid-1||user1@test.com||user1@mail.com\n" +
                "guid-2||user2@test.com||user2@mail.com";
        SshResponseHandler handler = new SshResponseHandler(exchangeSchema, response, dc);

        Set<Map<String, String>> result = handler.parseSearchOperation();

        Assert.assertEquals(result.size(), 2);
    }

    @Test
    public void testParseSearch_singlePipeSeparator() {
        DynamicConfiguration dc = exchangePersistent();
        String response = "ExchangeGuid|UserPrincipalName|email\nguid-1|user1@test.com|user1@mail.com";
        SshResponseHandler handler = new SshResponseHandler(exchangeSchema, response, dc);

        Set<Map<String, String>> result = handler.parseSearchOperation();

        Assert.assertEquals(result.size(), 1);
        Map<String, String> row = result.iterator().next();
        Assert.assertEquals(row.get("icfsUid"), "guid-1");
        Assert.assertEquals(row.get("icfsName"), "user1@test.com");
    }

    @Test
    public void testParseSearch_emptyAttribute_nullMarker() {
        DynamicConfiguration dc = exchangeBasic(); // emptyAttr = "null"
        String response = "ExchangeGuid||UserPrincipalName||email\nguid-1||user1@test.com||null";
        SshResponseHandler handler = new SshResponseHandler(exchangeSchema, response, dc);

        Set<Map<String, String>> result = handler.parseSearchOperation();
        Map<String, String> row = result.iterator().next();
        Assert.assertNull(row.get("email"));
    }

    @Test
    public void testParseSearch_emptyAttribute_nullValueMarker() {
        DynamicConfiguration dc = exchangePersistent(); // emptyAttr = "__NULL_VALUE__"
        String response = "ExchangeGuid|UserPrincipalName|email\nguid-1|user1@test.com|__NULL_VALUE__";
        SshResponseHandler handler = new SshResponseHandler(exchangeSchema, response, dc);

        Set<Map<String, String>> result = handler.parseSearchOperation();
        Map<String, String> row = result.iterator().next();
        Assert.assertNull(row.get("email"));
    }

    @Test
    public void testParseSearch_emptyString_mapsToNull() {
        DynamicConfiguration dc = exchangeBasic();
        String response = "ExchangeGuid||UserPrincipalName||email\nguid-1||user1@test.com||";
        SshResponseHandler handler = new SshResponseHandler(exchangeSchema, response, dc);

        Set<Map<String, String>> result = handler.parseSearchOperation();
        Map<String, String> row = result.iterator().next();
        Assert.assertNull(row.get("email"));
    }

    @Test
    public void testParseSearch_uidAndNameSame() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildMinimal("|", "\n", "null", "~");
        String response = "uid|login|fullName\njdoe|jdoe_login|John Doe";
        SshResponseHandler handler = new SshResponseHandler(openBsdSchema, response, dc);

        Set<Map<String, String>> result = handler.parseSearchOperation();
        Map<String, String> row = result.iterator().next();
        Assert.assertEquals(row.get("icfsUid"), "jdoe");
        Assert.assertEquals(row.get("icfsName"), "jdoe");
        Assert.assertEquals(row.get("login"), "jdoe_login");
        Assert.assertEquals(row.get("fullName"), "John Doe");
    }

    @Test
    public void testParseSearch_uidAndNameDifferent() {
        DynamicConfiguration dc = exchangeBasic();
        String response = "ExchangeGuid||UserPrincipalName||email\nguid-abc||admin@corp.com||admin@mail.com";
        SshResponseHandler handler = new SshResponseHandler(exchangeSchema, response, dc);

        Set<Map<String, String>> result = handler.parseSearchOperation();
        Map<String, String> row = result.iterator().next();
        Assert.assertEquals(row.get("icfsUid"), "guid-abc");
        Assert.assertEquals(row.get("icfsName"), "admin@corp.com");
    }

    @Test
    public void testParseSearch_attributeNotInSchema_ignored() {
        DynamicConfiguration dc = exchangeBasic();
        // "unknownAttr" is not in the exchange user schema
        String response = "ExchangeGuid||UserPrincipalName||email||unknownAttr\nguid-1||user1@test.com||user1@mail.com||someValue";
        SshResponseHandler handler = new SshResponseHandler(exchangeSchema, response, dc);

        Set<Map<String, String>> result = handler.parseSearchOperation();
        Map<String, String> row = result.iterator().next();
        Assert.assertNull(row.get("unknownAttr"));
        Assert.assertEquals(row.get("email"), "user1@mail.com");
    }

    @Test
    public void testParseSearch_multiValuedSeparatorPreservedInRawValue() {
        DynamicConfiguration dc = exchangeBasic(); // mvSep = "~"
        String response = "ExchangeGuid||UserPrincipalName||emailAddresses\nguid-1||user1@test.com||addr1~addr2~addr3";
        SshResponseHandler handler = new SshResponseHandler(exchangeSchema, response, dc);

        Set<Map<String, String>> result = handler.parseSearchOperation();
        Map<String, String> row = result.iterator().next();
        // SshResponseHandler does NOT split multi-valued — that happens in UniversalObjectsHandler
        Assert.assertEquals(row.get("emailAddresses"), "addr1~addr2~addr3");
    }

    @Test(expectedExceptions = ConnectorException.class)
    public void testParseSearch_columnCountMismatch_throws() {
        DynamicConfiguration dc = exchangeBasic();
        String response = "ExchangeGuid||UserPrincipalName||email\nguid-1||user1@test.com";
        SshResponseHandler handler = new SshResponseHandler(exchangeSchema, response, dc);
        handler.parseSearchOperation();
    }

    @Test(expectedExceptions = InvalidAttributeValueException.class)
    public void testParseSearch_emptyIcfsUid_throws() {
        DynamicConfiguration dc = exchangeBasic();
        String response = "ExchangeGuid||UserPrincipalName||email\n||user1@test.com||user1@mail.com";
        SshResponseHandler handler = new SshResponseHandler(exchangeSchema, response, dc);
        handler.parseSearchOperation();
    }

    @Test(expectedExceptions = InvalidAttributeValueException.class)
    public void testParseSearch_emptyIcfsName_throws() {
        DynamicConfiguration dc = exchangeBasic();
        String response = "ExchangeGuid||UserPrincipalName||email\nguid-1||||user1@mail.com";
        SshResponseHandler handler = new SshResponseHandler(exchangeSchema, response, dc);
        handler.parseSearchOperation();
    }

    @Test
    public void testHandleResponse_updateSuccess_returnsNull() {
        DynamicConfiguration dc = exchangeBasic(); // updateSuccessResponse = ""
        SshResponseHandler handler = new SshResponseHandler(exchangeSchema, "", dc);
        String result = handler.handleSearchOrUpdateOrDeleteResponse();
        Assert.assertNull(result);
    }

    @Test
    public void testHandleResponse_deleteSuccess_returnsNull() {
        DynamicConfiguration dc = exchangeBasic(); // deleteSuccessResponse = ""
        SshResponseHandler handler = new SshResponseHandler(exchangeSchema, "", dc);
        String result = handler.handleSearchOrUpdateOrDeleteResponse();
        Assert.assertNull(result);
    }

    @Test(expectedExceptions = UnknownUidException.class)
    public void testHandleResponse_unknownUid_throws() {
        DynamicConfiguration dc = exchangeBasic();
        SshResponseHandler handler = new SshResponseHandler(exchangeSchema, "UnknownUid", dc);
        handler.handleSearchOrUpdateOrDeleteResponse();
    }

    @Test(expectedExceptions = UnknownUidException.class)
    public void testHandleResponse_unknownUid_embeddedInLargerResponse_throws() {
        DynamicConfiguration dc = exchangeBasic();
        SshResponseHandler handler = new SshResponseHandler(exchangeSchema, "Error: UnknownUid object not found in directory", dc);
        handler.handleSearchOrUpdateOrDeleteResponse();
    }

    @Test
    public void testHandleResponse_otherError_returnsRawResponse() {
        DynamicConfiguration dc = exchangeBasic();
        String error = "Some unexpected error occurred";
        SshResponseHandler handler = new SshResponseHandler(exchangeSchema, error, dc);
        String result = handler.handleSearchOrUpdateOrDeleteResponse();
        Assert.assertEquals(result, error);
    }

    @Test
    public void testParseCreate_validResponse_returnsUid() {
        DynamicConfiguration dc = exchangeBasic();
        String response = "ExchangeGuid||UserPrincipalName\nguid-new||newuser@test.com";
        SshResponseHandler handler = new SshResponseHandler(exchangeSchema, response, dc);

        Uid uid = handler.parseCreateOperation();
        Assert.assertEquals(uid.getUidValue(), "guid-new");
    }

    @Test
    public void testParseCreate_uidAndNameSame_returnsUid() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildMinimal("|", "\n", "null", "~");
        String response = "uid|login\njdoe|jdoe_login";
        SshResponseHandler handler = new SshResponseHandler(openBsdSchema, response, dc);

        Uid uid = handler.parseCreateOperation();
        Assert.assertEquals(uid.getUidValue(), "jdoe");
    }

    @Test(expectedExceptions = NoCreateScriptResponseException.class)
    public void testParseCreate_emptyResponse_throws() {
        DynamicConfiguration dc = exchangeBasic();
        SshResponseHandler handler = new SshResponseHandler(exchangeSchema, "", dc);
        handler.parseCreateOperation();
    }

    @Test(expectedExceptions = AlreadyExistsException.class)
    public void testParseCreate_alreadyExists_throws() {
        DynamicConfiguration dc = exchangeBasic();
        SshResponseHandler handler = new SshResponseHandler(exchangeSchema, "ObjectAlreadyExists: user already present", dc);
        handler.parseCreateOperation();
    }

    @Test(expectedExceptions = InvalidCreateScriptOutputException.class)
    public void testParseCreate_missingIcfsName_throws() {
        DynamicConfiguration dc = exchangeBasic();
        // Header says "ExchangeGuid||someOther" — missing "UserPrincipalName"
        String response = "ExchangeGuid||someOther\nguid-1||value";
        SshResponseHandler handler = new SshResponseHandler(exchangeSchema, response, dc);
        handler.parseCreateOperation();
    }

    @Test(expectedExceptions = InvalidCreateScriptOutputException.class)
    public void testParseCreate_missingIcfsUid_throws() {
        DynamicConfiguration dc = exchangeBasic();
        // Header says "UserPrincipalName||someOther" — missing "ExchangeGuid"
        String response = "UserPrincipalName||someOther\nuser1@test.com||value";
        SshResponseHandler handler = new SshResponseHandler(exchangeSchema, response, dc);
        handler.parseCreateOperation();
    }
}
