package com.inalogy.midpoint.connectors.ssh;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeDelta;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.PoolableConnector;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateDeltaOp;

import java.util.Set;

@ConnectorClass(displayNameKey = "shh.connector.display", configurationClass = SshConfiguration.class)
public class SshConnector extends com.evolveum.polygon.connector.ssh.SshConnector implements Connector,
        PoolableConnector,
        SchemaOp,
        TestOp,
        SearchOp<Filter>,
        CreateOp,
        UpdateDeltaOp,
        DeleteOp {

    private static final Log LOG = Log.getLog(SshConnector.class);

    @Override
    public void init(Configuration configuration) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public void test() {

    }

    @Override
    public Schema schema() {
        return null;
    }

    @Override
    public FilterTranslator<Filter> createFilterTranslator(ObjectClass objectClass, OperationOptions options) {
        return null;
    }

    @Override
    public void executeQuery(ObjectClass objectClass, Filter query, ResultsHandler handler, OperationOptions options) {

    }

    @Override
    public Uid create(ObjectClass objectClass, Set<Attribute> createAttributes, OperationOptions options) {
        return null;
    }

    @Override
    public Set<AttributeDelta> updateDelta(ObjectClass objclass, Uid uid, Set<AttributeDelta> modifications, OperationOptions options) {
        return null;
    }

    @Override
    public void delete(ObjectClass objectClass, Uid uid, OperationOptions options) {

    }
}


