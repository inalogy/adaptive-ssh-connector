package com.inalogy.midpoint.connectors.ssh;


import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestClient {

    private static final Log LOG = Log.getLog(TestClient.class);
    private static SshConnectorConfiguration conf;
    private static SshConnector conn;

    @Test
    public void testConn() throws Exception {
        // TODO
    }

    @Test
    public void testSchema() throws Exception {
        // TODO
    }

    @Test
    public void testParser () throws Exception{
        // TODO
    }
}