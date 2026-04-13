package com.inalogy.midpoint.connectors.ssh.integration;

import com.inalogy.midpoint.connectors.ssh.AdaptiveSshConfiguration;
import com.inalogy.midpoint.connectors.ssh.AdaptiveSshConnector;
import com.inalogy.midpoint.connectors.ssh.exceptions.InvalidCreateScriptOutputException;
import com.inalogy.midpoint.connectors.ssh.exceptions.NoCreateScriptResponseException;
import com.inalogy.midpoint.connectors.ssh.filter.SshFilter;
import com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig.DynamicConfiguration;
import com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig.DynamicConfigurationTestBuilder;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@Test(groups = "integration", singleThreaded = true)
public class ConnectorExecModeIntegrationTest {

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
    public void resetDynConfig() {
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

    private AdaptiveSshConnector createConnector() {
        AdaptiveSshConnector connector = new AdaptiveSshConnector();
        connector.init(createConfig());
        return connector;
    }

    @Test
    public void testTest_echoHello_succeeds() {
        server.registerCommandHandler("echo", cmd -> "Hello");
        AdaptiveSshConnector connector = createConnector();
        try {
            connector.test();
        } finally {
            connector.dispose();
        }
    }

    @Test
    public void testSchema_parsesCorrectly() {
        server.registerCommandHandler("echo", cmd -> "Hello");
        AdaptiveSshConnector connector = createConnector();
        try {
            Schema schema = connector.schema();
            Assert.assertNotNull(schema);
            Assert.assertFalse(schema.getObjectClassInfo().isEmpty());

            boolean hasUser = false;
            boolean hasGroup = false;
            for (ObjectClassInfo oci : schema.getObjectClassInfo()) {
                if ("user".equals(oci.getType())) hasUser = true;
                if ("group".equals(oci.getType())) hasGroup = true;
            }
            Assert.assertTrue(hasUser);
            Assert.assertTrue(hasGroup);
        } finally {
            connector.dispose();
        }
    }

    @Test
    public void testSearch_allObjects_returnsMultiple() {
        server.registerCommandHandler("echo", cmd -> "Hello");
        server.registerCommandHandler("searchScript", cmd ->
                "ExchangeGuid||UserPrincipalName||email\n" +
                "guid-1||user1@test.com||user1@mail.com\n" +
                "guid-2||user2@test.com||user2@mail.com"
        );

        AdaptiveSshConnector connector = createConnector();
        try {
            List<ConnectorObject> results = new ArrayList<>();
            connector.executeQuery(
                    new ObjectClass("user"),
                    null,
                    results::add,
                    new OperationOptionsBuilder().build()
            );

            Assert.assertEquals(results.size(), 2);
        } finally {
            connector.dispose();
        }
    }

    @Test
    public void testSearch_byUid_returnsSingleObject() {
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
            Assert.assertEquals(results.get(0).getName().getNameValue(), "user1@test.com");
        } finally {
            connector.dispose();
        }
    }

    @Test
    public void testSearch_byName_returnsSingleObject() {
        server.registerCommandHandler("echo", cmd -> "Hello");
        server.registerCommandHandler("searchScript", cmd ->
                "ExchangeGuid||UserPrincipalName||email\nguid-1||user1@test.com||user1@mail.com"
        );

        AdaptiveSshConnector connector = createConnector();
        try {
            SshFilter filter = new SshFilter();
            filter.byName = "user1@test.com";
            List<ConnectorObject> results = new ArrayList<>();
            connector.executeQuery(
                    new ObjectClass("user"),
                    filter,
                    results::add,
                    new OperationOptionsBuilder().build()
            );

            Assert.assertEquals(results.size(), 1);
        } finally {
            connector.dispose();
        }
    }

    @Test
    public void testSearch_noResults_returnsEmpty() {
        server.registerCommandHandler("echo", cmd -> "Hello");
        // noResultSuccessMessage is "" for exchange basic — empty response = no results
        server.registerCommandHandler("searchScript", cmd -> "");

        AdaptiveSshConnector connector = createConnector();
        try {
            SshFilter filter = new SshFilter();
            filter.byUid = "nonexistent";
            List<ConnectorObject> results = new ArrayList<>();
            connector.executeQuery(
                    new ObjectClass("user"),
                    filter,
                    results::add,
                    new OperationOptionsBuilder().build()
            );

            Assert.assertTrue(results.isEmpty());
        } finally {
            connector.dispose();
        }
    }

    @Test
    public void testCreate_validAttributes_returnsUid() {
        server.registerCommandHandler("echo", cmd -> "Hello");
        server.registerCommandHandler("createScript", cmd ->
                "ExchangeGuid||UserPrincipalName\nguid-new||newuser@test.com"
        );

        AdaptiveSshConnector connector = createConnector();
        try {
            Set<Attribute> attrs = new HashSet<>();
            attrs.add(AttributeBuilder.build("__NAME__", "newuser@test.com"));
            attrs.add(AttributeBuilder.build("email", "newuser@mail.com"));

            Uid uid = connector.create(new ObjectClass("user"), attrs, new OperationOptionsBuilder().build());

            Assert.assertNotNull(uid);
            Assert.assertEquals(uid.getUidValue(), "guid-new");
        } finally {
            connector.dispose();
        }
    }

    @Test(expectedExceptions = AlreadyExistsException.class)
    public void testCreate_alreadyExists_throws() {
        server.registerCommandHandler("echo", cmd -> "Hello");
        server.registerCommandHandler("createScript", cmd -> "ObjectAlreadyExists: user@test.com");

        AdaptiveSshConnector connector = createConnector();
        try {
            Set<Attribute> attrs = new HashSet<>();
            attrs.add(AttributeBuilder.build("__NAME__", "existing@test.com"));
            connector.create(new ObjectClass("user"), attrs, new OperationOptionsBuilder().build());
        } finally {
            connector.dispose();
        }
    }

    @Test(expectedExceptions = NoCreateScriptResponseException.class)
    public void testCreate_emptyResponse_throws() {
        server.registerCommandHandler("echo", cmd -> "Hello");
        server.registerCommandHandler("createScript", cmd -> "");

        AdaptiveSshConnector connector = createConnector();
        try {
            Set<Attribute> attrs = new HashSet<>();
            attrs.add(AttributeBuilder.build("__NAME__", "newuser@test.com"));
            connector.create(new ObjectClass("user"), attrs, new OperationOptionsBuilder().build());
        } finally {
            connector.dispose();
        }
    }

    @Test
    public void testUpdateDelta_singleValue_succeeds() {
        server.registerCommandHandler("echo", cmd -> "Hello");
        server.registerCommandHandler("updateScript", cmd -> "");

        AdaptiveSshConnector connector = createConnector();
        try {
            Set<AttributeDelta> mods = new HashSet<>();
            mods.add(AttributeDeltaBuilder.build("email", "updated@test.com"));

            Set<AttributeDelta> result = connector.updateDelta(
                    new ObjectClass("user"),
                    new Uid("guid-1"),
                    mods,
                    new OperationOptionsBuilder().build()
            );

            Assert.assertNull(result);
        } finally {
            connector.dispose();
        }
    }

    @Test
    public void testUpdateDelta_nullValue_sendsEmptyMarker() {
        server.registerCommandHandler("echo", cmd -> "Hello");
        server.registerCommandHandler("updateScript", cmd -> {
            // Verify the command contains the empty marker "null"
            Assert.assertTrue(cmd.contains("\"null\""), "Command should contain empty marker: " + cmd);
            return "";
        });

        AdaptiveSshConnector connector = createConnector();
        try {
            Set<AttributeDelta> mods = new HashSet<>();
            // Replace with null = empty replace
            mods.add(AttributeDeltaBuilder.build("email"));

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
    public void testUpdateDelta_multivalued_addAndRemove() {
        server.registerCommandHandler("echo", cmd -> "Hello");
        server.registerCommandHandler("updateScript", cmd -> {
            Assert.assertTrue(cmd.contains("ADD:new@test.com"), "Should contain ADD prefix: " + cmd);
            Assert.assertTrue(cmd.contains("REMOVE:old@test.com"), "Should contain REMOVE prefix: " + cmd);
            return "";
        });

        AdaptiveSshConnector connector = createConnector();
        try {
            Set<AttributeDelta> mods = new HashSet<>();
            mods.add(AttributeDeltaBuilder.build(
                    "emailAddresses",
                    Arrays.asList("new@test.com"),
                    Arrays.asList("old@test.com")
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
    public void testDelete_succeeds() {
        server.registerCommandHandler("echo", cmd -> "Hello");
        server.registerCommandHandler("deleteScript", cmd -> "");

        AdaptiveSshConnector connector = createConnector();
        try {
            connector.delete(
                    new ObjectClass("user"),
                    new Uid("guid-1"),
                    new OperationOptionsBuilder().build()
            );
        } finally {
            connector.dispose();
        }
    }

    @Test(expectedExceptions = UnknownUidException.class)
    public void testDelete_unknownUid_throws() {
        server.registerCommandHandler("echo", cmd -> "Hello");
        server.registerCommandHandler("deleteScript", cmd -> "UnknownUid: guid-999 not found");

        AdaptiveSshConnector connector = createConnector();
        try {
            connector.delete(
                    new ObjectClass("user"),
                    new Uid("guid-999"),
                    new OperationOptionsBuilder().build()
            );
        } finally {
            connector.dispose();
        }
    }

    @Test(expectedExceptions = ConnectorException.class)
    public void testSearch_fatalError_throwsConnectorException() {
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
}
