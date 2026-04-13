package com.inalogy.midpoint.connectors.ssh.unit;

import com.inalogy.midpoint.connectors.ssh.AdaptiveSshConfiguration;

import org.identityconnectors.common.security.GuardedString;
import org.testng.annotations.Test;

import java.nio.file.Paths;

@Test(groups = "unit")
public class AdaptiveSshConfigurationTest {

    private String schemaPath() {
        return Paths.get("src/test/resources/unit/schema-basic.json").toAbsolutePath().toString();
    }

    private String dynConfigPath() {
        return Paths.get("src/test/resources/unit/dynconfig-exchange-basic.json").toAbsolutePath().toString();
    }

    private AdaptiveSshConfiguration validConfig() {
        AdaptiveSshConfiguration cfg = new AdaptiveSshConfiguration();
        cfg.setHost("localhost");
        cfg.setPort(22);
        cfg.setUsername("testuser");
        cfg.setPassword(new GuardedString("testpass".toCharArray()));
        cfg.setSchemaFilePath(schemaPath());
        cfg.setDynamicConfigurationFilePath(dynConfigPath());
        cfg.setAuthenticationScheme("password");
        cfg.setRemoteCharset("UTF-8");
        cfg.setConnectTimeout(10);
        return cfg;
    }

    @Test
    public void testValidate_validPasswordAuth_succeeds() {
        validConfig().validate();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidate_nullHost_throws() {
        AdaptiveSshConfiguration cfg = validConfig();
        cfg.setHost(null);
        cfg.validate();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidate_blankHost_throws() {
        AdaptiveSshConfiguration cfg = validConfig();
        cfg.setHost("   ");
        cfg.validate();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidate_portZero_throws() {
        AdaptiveSshConfiguration cfg = validConfig();
        cfg.setPort(0);
        cfg.validate();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidate_portNegative_throws() {
        AdaptiveSshConfiguration cfg = validConfig();
        cfg.setPort(-1);
        cfg.validate();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidate_portExceedsMax_throws() {
        AdaptiveSshConfiguration cfg = validConfig();
        cfg.setPort(70000);
        cfg.validate();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidate_nullUsername_throws() {
        AdaptiveSshConfiguration cfg = validConfig();
        cfg.setUsername(null);
        cfg.validate();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidate_nullSchemaFilePath_throws() {
        AdaptiveSshConfiguration cfg = validConfig();
        cfg.setSchemaFilePath(null);
        cfg.validate();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidate_nonExistentSchemaFile_throws() {
        AdaptiveSshConfiguration cfg = validConfig();
        cfg.setSchemaFilePath("/nonexistent/schema.json");
        cfg.validate();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidate_nullDynamicConfigPath_throws() {
        AdaptiveSshConfiguration cfg = validConfig();
        cfg.setDynamicConfigurationFilePath(null);
        cfg.validate();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidate_nonExistentDynamicConfigFile_throws() {
        AdaptiveSshConfiguration cfg = validConfig();
        cfg.setDynamicConfigurationFilePath("/nonexistent/config.json");
        cfg.validate();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidate_passwordAuth_nullPassword_throws() {
        AdaptiveSshConfiguration cfg = validConfig();
        cfg.setPassword(null);
        cfg.validate();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidate_unsupportedAuthScheme_throws() {
        AdaptiveSshConfiguration cfg = validConfig();
        cfg.setAuthenticationScheme("kerberos");
        cfg.validate();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidate_unsupportedCharset_throws() {
        AdaptiveSshConfiguration cfg = validConfig();
        cfg.setRemoteCharset("EBCDIC");
        cfg.validate();
    }

    @Test
    public void testValidate_allSupportedCharsets_succeed() {
        for (String charset : AdaptiveSshConfiguration.SUPPORTED_CHARSETS) {
            AdaptiveSshConfiguration cfg = validConfig();
            cfg.setRemoteCharset(charset);
            cfg.validate();
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidate_connectTimeoutZero_throws() {
        AdaptiveSshConfiguration cfg = validConfig();
        cfg.setConnectTimeout(0);
        cfg.validate();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidate_connectTimeoutTooHigh_throws() {
        AdaptiveSshConfiguration cfg = validConfig();
        cfg.setConnectTimeout(301);
        cfg.validate();
    }

    @Test
    public void testValidate_connectTimeoutBoundary_succeeds() {
        AdaptiveSshConfiguration cfg = validConfig();
        cfg.setConnectTimeout(1);
        cfg.validate();

        cfg.setConnectTimeout(300);
        cfg.validate();
    }
}
