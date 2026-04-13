package com.inalogy.midpoint.connectors.ssh.integration;

import com.inalogy.midpoint.connectors.ssh.AdaptiveSshConfiguration;
import com.inalogy.midpoint.connectors.ssh.cmd.SessionManager;
import com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig.DynamicConfiguration;
import com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig.DynamicConfigurationTestBuilder;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.IOException;
import java.nio.file.Paths;

@Test(groups = "integration", singleThreaded = true)
public class SessionManagerIntegrationTest {

    private static EmbeddedSshServer server;

    @BeforeClass
    public void startServer() throws IOException {
        server = new EmbeddedSshServer();
        server.start();
    }

    @AfterClass
    public void stopServer() throws IOException {
        server.stop();
    }

    @BeforeMethod
    public void resetHandlers() {
        server.clearHandlers();
        DynamicConfigurationTestBuilder.reset();
    }

    @AfterMethod
    public void tearDown() {
        DynamicConfigurationTestBuilder.reset();
    }

    private AdaptiveSshConfiguration createConfig() {
        AdaptiveSshConfiguration cfg = new AdaptiveSshConfiguration();
        cfg.setHost("localhost");
        cfg.setPort(server.getPort());
        cfg.setUsername(EmbeddedSshServer.TEST_USER);
        cfg.setPassword(new GuardedString(EmbeddedSshServer.TEST_PASS.toCharArray()));
        cfg.setShellType("shell");
        cfg.setArgumentStyle("dash");
        cfg.setSchemaFilePath(Paths.get("src/test/resources/unit/schema-basic.json").toAbsolutePath().toString());
        cfg.setDynamicConfigurationFilePath(Paths.get("src/test/resources/unit/dynconfig-exchange-basic.json").toAbsolutePath().toString());
        cfg.setConnectTimeout(5);
        cfg.setSshResponseTimeout(5);
        return cfg;
    }

    @Test
    public void testInitSshClient_connects() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        SessionManager sm = new SessionManager(createConfig(), dc);
        sm.initSshClient();
        try {
            Assert.assertTrue(sm.isConnectionAlive());
        } finally {
            sm.disposeSshClient();
        }
    }

    @Test
    public void testDisposeSshClient_disconnects() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        SessionManager sm = new SessionManager(createConfig(), dc);
        sm.initSshClient();
        sm.disposeSshClient();
        Assert.assertFalse(sm.isConnectionAlive());
    }

    @Test
    public void testExec_simpleEcho() {
        server.registerCommandHandler("echo", cmd -> "Hello");
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        SessionManager sm = new SessionManager(createConfig(), dc);
        sm.initSshClient();
        try {
            String result = sm.exec("echo Hello");
            Assert.assertEquals(result, "Hello");
        } finally {
            sm.disposeSshClient();
        }
    }

    @Test
    public void testExec_commandWithResponse() {
        server.registerCommandHandler("search", cmd -> "guid-1||user1@test.com||user1@mail.com");
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        SessionManager sm = new SessionManager(createConfig(), dc);
        sm.initSshClient();
        try {
            String result = sm.exec("sh /scripts/search.sh -name user1");
            Assert.assertTrue(result.contains("guid-1"));
        } finally {
            sm.disposeSshClient();
        }
    }

    @Test(expectedExceptions = ConnectorException.class)
    public void testExec_fatalError_throwsConnectorException() {
        server.registerCommandHandler("search", cmd -> "__FATAL_ERROR__");
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        SessionManager sm = new SessionManager(createConfig(), dc);
        sm.initSshClient();
        try {
            sm.exec("sh /scripts/search.sh");
        } finally {
            sm.disposeSshClient();
        }
    }
}
