package com.inalogy.midpoint.connectors.ssh.unit;

import com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig.DynamicConfiguration;
import com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig.DynamicConfigurationTestBuilder;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.nio.file.Paths;

@Test(groups = "unit")
public class DynamicConfigurationTest {

    @AfterMethod
    public void tearDown() {
        DynamicConfigurationTestBuilder.reset();
    }

    private String resourcePath(String filename) {
        return Paths.get("src/test/resources/unit", filename).toAbsolutePath().toString();
    }

    @Test
    public void testInit_exchangeBasicConfig() {
        DynamicConfigurationTestBuilder.reset();
        DynamicConfiguration dc = DynamicConfiguration.getInstance();
        dc.init(resourcePath("dynconfig-exchange-basic.json"));

        Assert.assertTrue(dc.isInitialized());
        Assert.assertEquals(dc.getConfigName(), "Test Configuration Exchange Basic");
        Assert.assertNotNull(dc.getSettings());
        Assert.assertNotNull(dc.getSettings().getScriptResponseSettings());
        Assert.assertNotNull(dc.getSettings().getConnectorSettings());
        Assert.assertNotNull(dc.getSettings().getCreateOperationSettings());
        Assert.assertNotNull(dc.getSettings().getUpdateOperationSettings());
        Assert.assertNotNull(dc.getSettings().getDeleteOperationSettings());
        Assert.assertNotNull(dc.getSettings().getSearchOperationSettings());

        Assert.assertEquals(dc.getSettings().getScriptResponseSettings().getResponseColumnSeparator(), "||");
        Assert.assertEquals(dc.getSettings().getScriptResponseSettings().getMultiValuedAttributeSeparator(), "~");
        Assert.assertEquals(dc.getSettings().getScriptResponseSettings().getScriptEmptyAttribute(), "null");
        Assert.assertEquals(dc.getSettings().getScriptResponseSettings().getResponseNewLineSeparator(), "\n");
    }

    @Test
    public void testInit_openbsdConfig() {
        DynamicConfigurationTestBuilder.reset();
        DynamicConfiguration dc = DynamicConfiguration.getInstance();
        dc.init(resourcePath("dynconfig-openbsd.json"));

        Assert.assertTrue(dc.isInitialized());
        Assert.assertTrue(dc.isReplaceWhiteSpaceEnabled());
        Assert.assertEquals(dc.getReplaceWhiteSpaceValue(), "---");
        Assert.assertTrue(dc.getSettings().getConnectorSettings().getIcfsPasswordFlagEquivalent().isEnabled());
        Assert.assertEquals(dc.getSettings().getConnectorSettings().getIcfsPasswordFlagEquivalent().getValue(), "password");
        Assert.assertEquals(dc.getSettings().getScriptResponseSettings().getResponseColumnSeparator(), "|");
    }

    @Test
    public void testInit_nullPath_doesNothing() {
        DynamicConfigurationTestBuilder.reset();
        DynamicConfiguration dc = DynamicConfiguration.getInstance();
        dc.init(null);

        Assert.assertFalse(dc.isInitialized());
        Assert.assertNull(dc.getSettings());
    }

    @Test
    public void testGetInstance_returnsSameInstance() {
        DynamicConfigurationTestBuilder.reset();
        DynamicConfiguration dc1 = DynamicConfiguration.getInstance();
        DynamicConfiguration dc2 = DynamicConfiguration.getInstance();
        Assert.assertSame(dc1, dc2);
    }

    @Test
    public void testResetInstance_createsFreshInstance() {
        DynamicConfigurationTestBuilder.buildExchangeBasic();
        DynamicConfiguration before = DynamicConfiguration.getInstance();
        Assert.assertNotNull(before.getSettings());

        DynamicConfigurationTestBuilder.reset();
        DynamicConfiguration after = DynamicConfiguration.getInstance();
        Assert.assertNull(after.getSettings());
        Assert.assertNotSame(before, after);
    }

    @Test
    public void testConvenienceMethods() {
        DynamicConfigurationTestBuilder.buildExchangeBasic();
        DynamicConfiguration dc = DynamicConfiguration.getInstance();

        Assert.assertEquals(dc.getScriptEmptyAttribute(), "null");
        Assert.assertFalse(dc.isReplaceWhiteSpaceEnabled());
        Assert.assertEquals(dc.getReplaceWhiteSpaceValue(), "---");
    }

    @Test
    public void testConvenienceMethods_openBsd() {
        DynamicConfigurationTestBuilder.buildOpenBsd();
        DynamicConfiguration dc = DynamicConfiguration.getInstance();

        Assert.assertEquals(dc.getScriptEmptyAttribute(), "null");
        Assert.assertTrue(dc.isReplaceWhiteSpaceEnabled());
        Assert.assertEquals(dc.getReplaceWhiteSpaceValue(), "---");
    }
}
