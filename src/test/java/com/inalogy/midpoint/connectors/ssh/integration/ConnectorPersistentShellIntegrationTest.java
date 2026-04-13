package com.inalogy.midpoint.connectors.ssh.integration;

import com.inalogy.midpoint.connectors.ssh.AdaptiveSshConfiguration;
import com.inalogy.midpoint.connectors.ssh.AdaptiveSshConnector;
import com.inalogy.midpoint.connectors.ssh.filter.SshFilter;
import com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig.DynamicConfiguration;
import com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig.DynamicConfigurationTestBuilder;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConnectionFailedException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@Test(groups = "integration", singleThreaded = true)
public class ConnectorPersistentShellIntegrationTest {

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

    private AdaptiveSshConfiguration createPersistentConfig() {
        AdaptiveSshConfiguration cfg = new AdaptiveSshConfiguration();
        cfg.setHost("localhost");
        cfg.setPort(server.getPort());
        cfg.setUsername(EmbeddedSshServer.TEST_USER);
        cfg.setPassword(new GuardedString(EmbeddedSshServer.TEST_PASS.toCharArray()));
        cfg.setShellType("shell");
        cfg.setArgumentStyle("dash");
        cfg.setUsePersistentShell(true);
        cfg.setSchemaFilePath(Paths.get("src/test/resources/unit/schema-basic.json").toAbsolutePath().toString());
        cfg.setDynamicConfigurationFilePath(Paths.get("src/test/resources/unit/dynconfig-exchange-basic.json").toAbsolutePath().toString());
        cfg.setConnectTimeout(5);
        cfg.setSshResponseTimeout(10);
        return cfg;
    }

    private AdaptiveSshConfiguration createPersistentConfigWithPreload() {
        AdaptiveSshConfiguration cfg = createPersistentConfig();
        cfg.setDynamicConfigurationFilePath(
                Paths.get("src/test/resources/unit/dynconfig-persistent-preload.json").toAbsolutePath().toString()
        );
        return cfg;
    }

    private AdaptiveSshConnector createConnector() {
        AdaptiveSshConnector connector = new AdaptiveSshConnector();
        connector.init(createPersistentConfig());
        return connector;
    }

    @Test
    public void testPersistentShell_preloadSuccess_thenCrudWorks() {
        // Simulate the real Exchange flow:
        // 1. preload.ps1 runs → connects to Exchange, imports commands, returns "BASE-OK"
        // 2. subsequent operations use the populated session ($Session variable alive)
        boolean[] preloadExecuted = {false};
        server.registerCommandHandler("preload.ps1", cmd -> {
            preloadExecuted[0] = true;
            return "BASE-OK";
        });
        server.registerCommandHandler("searchScript", cmd ->
                "ExchangeGuid||UserPrincipalName||email\nguid-1||user1@test.com||user1@mail.com"
        );

        AdaptiveSshConnector connector = new AdaptiveSshConnector();
        connector.init(createPersistentConfigWithPreload());
        try {
            // If we get here, preload succeeded — now do a search on the same session
            SshFilter filter = new SshFilter();
            filter.byUid = "guid-1";
            List<ConnectorObject> results = new ArrayList<>();
            connector.executeQuery(new ObjectClass("user"), filter, results::add, new OperationOptionsBuilder().build());

            Assert.assertTrue(preloadExecuted[0], "Preload script should have been executed");
            Assert.assertEquals(results.size(), 1);
            Assert.assertEquals(results.get(0).getUid().getUidValue(), "guid-1");
        } finally {
            connector.dispose();
        }
    }

    @Test
    public void testPersistentShell_preloadSuccess_fullCrudOnSameSession() {
        // Full Exchange-like flow: preload → create → search → update → delete
        // all on the same persistent session
        server.registerCommandHandler("preload.ps1", cmd -> "BASE-OK");
        server.registerCommandHandler("createScript", cmd ->
                "ExchangeGuid||UserPrincipalName\nguid-new||newuser@test.com"
        );
        server.registerCommandHandler("searchScript", cmd ->
                "ExchangeGuid||UserPrincipalName||email\nguid-new||newuser@test.com||new@mail.com"
        );
        server.registerCommandHandler("updateScript", cmd -> "");
        server.registerCommandHandler("deleteScript", cmd -> "");

        AdaptiveSshConnector connector = new AdaptiveSshConnector();
        connector.init(createPersistentConfigWithPreload());
        try {
            Set<Attribute> createAttrs = new HashSet<>();
            createAttrs.add(AttributeBuilder.build("__NAME__", "newuser@test.com"));
            createAttrs.add(AttributeBuilder.build("email", "new@mail.com"));
            Uid uid = connector.create(new ObjectClass("user"), createAttrs, new OperationOptionsBuilder().build());
            Assert.assertEquals(uid.getUidValue(), "guid-new");

            SshFilter filter = new SshFilter();
            filter.byUid = "guid-new";
            List<ConnectorObject> results = new ArrayList<>();
            connector.executeQuery(new ObjectClass("user"), filter, results::add, new OperationOptionsBuilder().build());
            Assert.assertEquals(results.size(), 1);

            Set<AttributeDelta> mods = new HashSet<>();
            mods.add(AttributeDeltaBuilder.build("email", "updated@mail.com"));
            connector.updateDelta(new ObjectClass("user"), uid, mods, new OperationOptionsBuilder().build());

            connector.delete(new ObjectClass("user"), uid, new OperationOptionsBuilder().build());
        } finally {
            connector.dispose();
        }
    }

    @Test(expectedExceptions = ConnectionFailedException.class)
    public void testPersistentShell_preloadReturnsWrongOutput_sessionClosed() {
        // Preload returns something other than "BASE-OK" — connector must close session
        server.registerCommandHandler("preload.ps1", cmd -> "ERROR: Connect-ExchangeOnline failed");

        AdaptiveSshConnector connector = new AdaptiveSshConnector();
        connector.init(createPersistentConfigWithPreload());
        // init() triggers startSession() → executePreloadScript() → mismatch → close + throw
        connector.dispose();
    }

    @Test(expectedExceptions = ConnectionFailedException.class)
    public void testPersistentShell_preloadReturnsEmpty_sessionClosed() {
        // Preload returns empty string instead of "BASE-OK"
        server.registerCommandHandler("preload.ps1", cmd -> "");

        AdaptiveSshConnector connector = new AdaptiveSshConnector();
        connector.init(createPersistentConfigWithPreload());
        connector.dispose();
    }

    @Test
    public void testPersistentShell_disposeScript_executedOnDispose() {
        // Full lifecycle: preload → operation → dispose
        // dispose.ps1 should run when connector.dispose() is called (e.g. Disconnect-ExchangeOnline)
        boolean[] disposeExecuted = {false};
        server.registerCommandHandler("preload.ps1", cmd -> "BASE-OK");
        server.registerCommandHandler("searchScript", cmd ->
                "ExchangeGuid||UserPrincipalName||email\nguid-1||user1@test.com||user1@mail.com"
        );
        server.registerCommandHandler("dispose.ps1", cmd -> {
            disposeExecuted[0] = true;
            return "";
        });

        AdaptiveSshConnector connector = new AdaptiveSshConnector();
        connector.init(createPersistentConfigWithPreload());
        try {
            SshFilter filter = new SshFilter();
            filter.byUid = "guid-1";
            List<ConnectorObject> results = new ArrayList<>();
            connector.executeQuery(new ObjectClass("user"), filter, results::add, new OperationOptionsBuilder().build());
            Assert.assertEquals(results.size(), 1);
        } finally {
            connector.dispose();
        }
        Assert.assertTrue(disposeExecuted[0], "Dispose script should have been executed on connector.dispose()");
    }

    @Test
    public void testPersistentShell_search_succeeds() {
        server.registerCommandHandler("echo", cmd -> "Hello");
        server.registerCommandHandler("searchScript", cmd ->
                "ExchangeGuid||UserPrincipalName||email\nguid-1||user1@test.com||user1@mail.com"
        );

        AdaptiveSshConnector connector = createConnector();
        try {
            SshFilter filter = new SshFilter();
            filter.byUid = "guid-1";
            List<ConnectorObject> results = new ArrayList<>();
            connector.executeQuery(
                    new ObjectClass("user"),
                    filter,
                    results::add,
                    new OperationOptionsBuilder().build()
            );

            Assert.assertEquals(results.size(), 1);
            Assert.assertEquals(results.get(0).getUid().getUidValue(), "guid-1");
        } finally {
            connector.dispose();
        }
    }

    @Test
    public void testPersistentShell_multipleSearches_reuseSession() {
        int[] searchCount = {0};
        server.registerCommandHandler("echo", cmd -> "Hello");
        server.registerCommandHandler("searchScript", cmd -> {
            searchCount[0]++;
            return "ExchangeGuid||UserPrincipalName||email\nguid-" + searchCount[0] + "||user" + searchCount[0] + "@test.com||user" + searchCount[0] + "@mail.com";
        });

        AdaptiveSshConnector connector = createConnector();
        try {
            // First search
            List<ConnectorObject> results1 = new ArrayList<>();
            SshFilter f1 = new SshFilter();
            f1.byUid = "guid-1";
            connector.executeQuery(new ObjectClass("user"), f1, results1::add, new OperationOptionsBuilder().build());
            Assert.assertEquals(results1.size(), 1);

            // Second search — should reuse the persistent shell
            List<ConnectorObject> results2 = new ArrayList<>();
            SshFilter f2 = new SshFilter();
            f2.byUid = "guid-2";
            connector.executeQuery(new ObjectClass("user"), f2, results2::add, new OperationOptionsBuilder().build());
            Assert.assertEquals(results2.size(), 1);

            // Both should have worked
            Assert.assertEquals(searchCount[0], 2, "Should have executed two search commands");
        } finally {
            connector.dispose();
        }
    }

    @Test
    public void testPersistentShell_fullCrudCycle() {
        server.registerCommandHandler("echo", cmd -> "Hello");
        server.registerCommandHandler("createScript", cmd ->
                "ExchangeGuid||UserPrincipalName\nguid-new||newuser@test.com"
        );
        server.registerCommandHandler("searchScript", cmd ->
                "ExchangeGuid||UserPrincipalName||email\nguid-new||newuser@test.com||new@mail.com"
        );
        server.registerCommandHandler("updateScript", cmd -> "");
        server.registerCommandHandler("deleteScript", cmd -> "");

        AdaptiveSshConnector connector = createConnector();
        try {
            // Create
            Set<Attribute> createAttrs = new HashSet<>();
            createAttrs.add(AttributeBuilder.build("__NAME__", "newuser@test.com"));
            createAttrs.add(AttributeBuilder.build("email", "new@mail.com"));
            Uid uid = connector.create(new ObjectClass("user"), createAttrs, new OperationOptionsBuilder().build());
            Assert.assertEquals(uid.getUidValue(), "guid-new");

            // Search
            SshFilter filter = new SshFilter();
            filter.byUid = "guid-new";
            List<ConnectorObject> results = new ArrayList<>();
            connector.executeQuery(new ObjectClass("user"), filter, results::add, new OperationOptionsBuilder().build());
            Assert.assertEquals(results.size(), 1);

            // Update
            Set<AttributeDelta> mods = new HashSet<>();
            mods.add(AttributeDeltaBuilder.build("email", "updated@mail.com"));
            connector.updateDelta(new ObjectClass("user"), uid, mods, new OperationOptionsBuilder().build());

            // Delete
            connector.delete(new ObjectClass("user"), uid, new OperationOptionsBuilder().build());
        } finally {
            connector.dispose();
        }
    }

    @Test(expectedExceptions = ConnectorException.class)
    public void testPersistentShell_fatalError_throwsAndClosesSession() {
        server.registerCommandHandler("echo", cmd -> "Hello");
        server.registerCommandHandler("searchScript", cmd -> "__FATAL_ERROR__");

        AdaptiveSshConnector connector = createConnector();
        try {
            connector.executeQuery(
                    new ObjectClass("user"),
                    null,
                    obj -> true,
                    new OperationOptionsBuilder().build()
            );
        } finally {
            connector.dispose();
        }
    }

    @Test
    public void testPersistentShell_updateDelta_multivalued() {
        server.registerCommandHandler("echo", cmd -> "Hello");
        server.registerCommandHandler("updateScript", cmd -> {
            Assert.assertTrue(cmd.contains("ADD:addr1@test.com"), "Should contain ADD: " + cmd);
            Assert.assertTrue(cmd.contains("REMOVE:addr2@test.com"), "Should contain REMOVE: " + cmd);
            return "";
        });

        AdaptiveSshConnector connector = createConnector();
        try {
            Set<AttributeDelta> mods = new HashSet<>();
            mods.add(AttributeDeltaBuilder.build(
                    "emailAddresses",
                    Arrays.asList("addr1@test.com"),
                    Arrays.asList("addr2@test.com")
            ));
            connector.updateDelta(
                    new ObjectClass("user"),
                    new Uid("guid-1"),
                    mods,
                    new OperationOptionsBuilder().build()
            );
        } finally {
            connector.dispose();
        }
    }

    @Test
    public void testPersistentShell_updateDelta_setToNull() {
        server.registerCommandHandler("echo", cmd -> "Hello");
        server.registerCommandHandler("updateScript", cmd -> {
            // Should contain the empty marker "null" for the email attribute
            Assert.assertTrue(cmd.contains("\"null\""), "Should contain null marker: " + cmd);
            return "";
        });

        AdaptiveSshConnector connector = createConnector();
        try {
            Set<AttributeDelta> mods = new HashSet<>();
            mods.add(AttributeDeltaBuilder.build("email")); // replace with nothing = null
            connector.updateDelta(
                    new ObjectClass("user"),
                    new Uid("guid-1"),
                    mods,
                    new OperationOptionsBuilder().build()
            );
        } finally {
            connector.dispose();
        }
    }

    @Test
    public void testNonPersistentMode_multipleOperations_eachGetsFreshSession() {
        // This test uses exec mode (non-persistent) with the same server
        int[] searchCount = {0};
        server.registerCommandHandler("echo", cmd -> "Hello");
        server.registerCommandHandler("searchScript", cmd -> {
            searchCount[0]++;
            return "ExchangeGuid||UserPrincipalName||email\nguid-" + searchCount[0] + "||user" + searchCount[0] + "@test.com||x@mail.com";
        });

        AdaptiveSshConfiguration cfg = createPersistentConfig();
        cfg.setUsePersistentShell(false); // non-persistent

        AdaptiveSshConnector connector = new AdaptiveSshConnector();
        connector.init(cfg);
        try {
            // First search
            List<ConnectorObject> r1 = new ArrayList<>();
            SshFilter f1 = new SshFilter();
            f1.byUid = "guid-1";
            connector.executeQuery(new ObjectClass("user"), f1, r1::add, new OperationOptionsBuilder().build());
            Assert.assertEquals(r1.size(), 1);

            // Second search — different session in non-persistent mode
            List<ConnectorObject> r2 = new ArrayList<>();
            SshFilter f2 = new SshFilter();
            f2.byUid = "guid-2";
            connector.executeQuery(new ObjectClass("user"), f2, r2::add, new OperationOptionsBuilder().build());
            Assert.assertEquals(r2.size(), 1);

            Assert.assertEquals(searchCount[0], 2);
        } finally {
            connector.dispose();
        }
    }
}
