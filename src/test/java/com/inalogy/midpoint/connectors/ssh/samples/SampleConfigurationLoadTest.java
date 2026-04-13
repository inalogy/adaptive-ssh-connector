package com.inalogy.midpoint.connectors.ssh.samples;

import com.inalogy.midpoint.connectors.ssh.schema.SchemaType;
import com.inalogy.midpoint.connectors.ssh.schema.UniversalSchemaHandler;
import com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig.DynamicConfiguration;
import com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig.DynamicConfigurationTestBuilder;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;

@Test(groups = "unit")
public class SampleConfigurationLoadTest {

    private static final String SAMPLES_DIR = Paths.get("samples").toAbsolutePath().toString();

    @AfterMethod
    public void tearDown() {
        DynamicConfigurationTestBuilder.reset();
    }

    @DataProvider(name = "schemaPaths")
    public Object[][] schemaPaths() {
        return new Object[][]{
                {"MicrosoftExchange/Exchange-OnPrem-basic", "schemaConfig.json"},
                {"MicrosoftExchange/Exchange-OnPrem-Persistent-Shell", "schemaConfiguration.json"},
                {"MicrosoftExchange/Exchange-Online-Persistent-Shell_DistLists-Management", "schemaConfiguration.json"},
                {"OpenBSD", "schemaConfig.json"},
        };
    }

    @Test(dataProvider = "schemaPaths")
    public void testLoadSampleSchema(String sampleDir, String schemaFile) {
        String path = SAMPLES_DIR + "/" + sampleDir + "/" + schemaFile;
        Assert.assertTrue(new File(path).exists(), "Schema file should exist: " + path);

        UniversalSchemaHandler handler = new UniversalSchemaHandler(path);
        Map<String, SchemaType> types = handler.getSchemaTypes();

        Assert.assertFalse(types.isEmpty(), "Should have at least one ObjectClass");
        for (SchemaType type : types.values()) {
            Assert.assertNotNull(type.getObjectClassName());
            Assert.assertNotNull(type.getIcfsUid());
            Assert.assertNotNull(type.getIcfsName());
            Assert.assertNotNull(type.getCreateScript());
            Assert.assertNotNull(type.getSearchScript());
            Assert.assertNotNull(type.getUpdateScript());
            Assert.assertNotNull(type.getDeleteScript());
        }
    }

    @DataProvider(name = "dynConfigPaths")
    public Object[][] dynConfigPaths() {
        return new Object[][]{
                {"MicrosoftExchange/Exchange-OnPrem-basic", "dynamicConfiguration.json"},
                {"MicrosoftExchange/Exchange-OnPrem-Persistent-Shell", "dynamicConfiguration.json"},
                {"MicrosoftExchange/Exchange-Online-Persistent-Shell_DistLists-Management", "dynamicConfiguration.json"},
                {"OpenBSD", "dynamicConfiguration.json"},
        };
    }

    @Test(dataProvider = "dynConfigPaths")
    public void testLoadSampleDynamicConfiguration(String sampleDir, String configFile) {
        DynamicConfigurationTestBuilder.reset();
        String path = SAMPLES_DIR + "/" + sampleDir + "/" + configFile;
        Assert.assertTrue(new File(path).exists(), "Config file should exist: " + path);

        DynamicConfiguration dc = DynamicConfiguration.getInstance();
        dc.init(path);

        Assert.assertTrue(dc.isInitialized());
        Assert.assertNotNull(dc.getSettings());
        Assert.assertNotNull(dc.getSettings().getScriptResponseSettings());
        Assert.assertNotNull(dc.getSettings().getConnectorSettings());
        Assert.assertNotNull(dc.getSettings().getCreateOperationSettings());
        Assert.assertNotNull(dc.getSettings().getUpdateOperationSettings());
        Assert.assertNotNull(dc.getSettings().getDeleteOperationSettings());
        Assert.assertNotNull(dc.getSettings().getSearchOperationSettings());
        Assert.assertNotNull(dc.getSettings().getScriptResponseSettings().getResponseColumnSeparator());
        Assert.assertNotNull(dc.getSettings().getScriptResponseSettings().getMultiValuedAttributeSeparator());
        Assert.assertNotNull(dc.getSettings().getScriptResponseSettings().getScriptEmptyAttribute());
    }

    @Test
    public void testSample_exchangeOnPremBasic() {
        String schemaPath = SAMPLES_DIR + "/MicrosoftExchange/Exchange-OnPrem-basic/schemaConfig.json";
        UniversalSchemaHandler handler = new UniversalSchemaHandler(schemaPath);

        Assert.assertEquals(handler.getSchemaTypes().size(), 2);
        Assert.assertTrue(handler.getSchemaTypes().containsKey("user"));
        Assert.assertTrue(handler.getSchemaTypes().containsKey("group"));

        SchemaType user = handler.getSchemaTypes().get("user");
        Assert.assertEquals(user.getIcfsUid(), "ExchangeGuid");
        Assert.assertEquals(user.getIcfsName(), "UserPrincipalName");
        Assert.assertFalse(user.isUidAndNameSame());

        DynamicConfigurationTestBuilder.reset();
        DynamicConfiguration dc = DynamicConfiguration.getInstance();
        dc.init(SAMPLES_DIR + "/MicrosoftExchange/Exchange-OnPrem-basic/dynamicConfiguration.json");

        Assert.assertEquals(dc.getSettings().getScriptResponseSettings().getResponseColumnSeparator(), "||");
        Assert.assertEquals(dc.getSettings().getScriptResponseSettings().getMultiValuedAttributeSeparator(), "~");
        Assert.assertEquals(dc.getSettings().getScriptResponseSettings().getScriptEmptyAttribute(), "null");
    }

    @Test
    public void testSample_exchangeOnPremPersistent() {
        String schemaPath = SAMPLES_DIR + "/MicrosoftExchange/Exchange-OnPrem-Persistent-Shell/schemaConfiguration.json";
        UniversalSchemaHandler handler = new UniversalSchemaHandler(schemaPath);

        Assert.assertEquals(handler.getSchemaTypes().size(), 4);
        Assert.assertTrue(handler.getSchemaTypes().containsKey("MailboxOnPrem"));
        Assert.assertTrue(handler.getSchemaTypes().containsKey("MailboxCloud"));
        Assert.assertTrue(handler.getSchemaTypes().containsKey("MailUser"));
        Assert.assertTrue(handler.getSchemaTypes().containsKey("MailContact"));

        DynamicConfigurationTestBuilder.reset();
        DynamicConfiguration dc = DynamicConfiguration.getInstance();
        dc.init(SAMPLES_DIR + "/MicrosoftExchange/Exchange-OnPrem-Persistent-Shell/dynamicConfiguration.json");

        Assert.assertEquals(dc.getSettings().getScriptResponseSettings().getResponseColumnSeparator(), "|");
        Assert.assertEquals(dc.getSettings().getScriptResponseSettings().getScriptEmptyAttribute(), "__NULL_VALUE__");
        Assert.assertTrue(dc.getSettings().getConnectorSettings().getPreloadScript().isEnabled());
        Assert.assertTrue(dc.getSettings().getConnectorSettings().getDisposeScript().isEnabled());
        Assert.assertEquals(dc.getSettings().getSearchOperationSettings().getGeneralFatalErrorMessage(), "__FATAL_ERROR__");
    }

    @Test
    public void testSample_exchangeOnlineDistLists() {
        String schemaPath = SAMPLES_DIR + "/MicrosoftExchange/Exchange-Online-Persistent-Shell_DistLists-Management/schemaConfiguration.json";
        UniversalSchemaHandler handler = new UniversalSchemaHandler(schemaPath);

        Assert.assertEquals(handler.getSchemaTypes().size(), 2);
        Assert.assertTrue(handler.getSchemaTypes().containsKey("EXOMailUser"));
        Assert.assertTrue(handler.getSchemaTypes().containsKey("EXODistributionGroup"));

        DynamicConfigurationTestBuilder.reset();
        DynamicConfiguration dc = DynamicConfiguration.getInstance();
        dc.init(SAMPLES_DIR + "/MicrosoftExchange/Exchange-Online-Persistent-Shell_DistLists-Management/dynamicConfiguration.json");

        Assert.assertEquals(dc.getSettings().getSearchOperationSettings().getNoResultSuccessMessage(), "__NO_RESULT__");
    }

    @Test
    public void testSample_openBSD() {
        String schemaPath = SAMPLES_DIR + "/OpenBSD/schemaConfig.json";
        UniversalSchemaHandler handler = new UniversalSchemaHandler(schemaPath);

        Assert.assertEquals(handler.getSchemaTypes().size(), 1);
        SchemaType user = handler.getSchemaTypes().get("user");
        Assert.assertEquals(user.getIcfsUid(), "uid");
        Assert.assertEquals(user.getIcfsName(), "uid");
        Assert.assertTrue(user.isUidAndNameSame());

        DynamicConfigurationTestBuilder.reset();
        DynamicConfiguration dc = DynamicConfiguration.getInstance();
        dc.init(SAMPLES_DIR + "/OpenBSD/dynamicConfiguration.json");

        Assert.assertTrue(dc.isReplaceWhiteSpaceEnabled());
        Assert.assertEquals(dc.getReplaceWhiteSpaceValue(), "---");
        Assert.assertTrue(dc.getSettings().getConnectorSettings().getIcfsPasswordFlagEquivalent().isEnabled());
        Assert.assertEquals(dc.getSettings().getConnectorSettings().getIcfsPasswordFlagEquivalent().getValue(), "password");
        Assert.assertEquals(dc.getSettings().getScriptResponseSettings().getResponseColumnSeparator(), "|");
    }
}
