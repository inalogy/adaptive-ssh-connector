package com.inalogy.midpoint.connectors.ssh;

import com.inalogy.midpoint.connectors.objects.UniversalObjectsHandler;
import com.inalogy.midpoint.connectors.schema.SchemaType;
import com.inalogy.midpoint.connectors.schema.UniversalSchemaHandler;
import com.inalogy.midpoint.connectors.utils.FileHashCalculator;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.*;
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

@ConnectorClass(displayNameKey = "ssh.connector.display", configurationClass = SshConfiguration.class)
public class SshConnector extends com.evolveum.polygon.connector.ssh.SshConnector implements Connector,
        PoolableConnector,
        SchemaOp,
        TestOp,
        SearchOp<Filter>,
        CreateOp,
        UpdateDeltaOp,
        DeleteOp {

    private SshConfiguration configuration;
    private static UniversalSchemaHandler schema = null;
    //Ssh Connector schema cache
    private static final Log LOG = Log.getLog(SshConnector.class);

    @Override
    public void init(Configuration configuration) {
        this.configuration = (SshConfiguration) configuration;
    }

    @Override
    public void dispose() {
        schema = null;

    }

    @Override
    public void test() {

    }

    @Override
    public Schema schema() {
        SchemaBuilder schemaBuilder = new SchemaBuilder(SshConnector.class);
        String currentFileSha256 = FileHashCalculator.calculateSHA256(this.configuration.getSchemaFilePath());

        if (schema == null){
            schema = new UniversalSchemaHandler(this.configuration.getSchemaFilePath());
//            LOG.info("Creating universalSchemaHandler schemaConfigFilePath: {}", this.configuration.getSchemaFilePath());

        }
        else if (schema != null && currentFileSha256 != null && !schema.getFileSha256().equals(currentFileSha256)){
            // if sha256 of schemaFile is unchanged we don't need to fetch it again
            LOG.info("Change in schemaConfigFile detected");
            schema = new UniversalSchemaHandler(this.configuration.getSchemaFilePath());
        }
        for (SchemaType schemaType: schema.getSchemaTypes().values()){
//            //TODO check if ok
            UniversalObjectsHandler.buildObjectClass(schemaBuilder, schemaType);
        }
        return schemaBuilder.build();
//        return null;
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
    public Set<AttributeDelta> updateDelta(ObjectClass objectClass, Uid uid, Set<AttributeDelta> modifications, OperationOptions options) {
        return null;
    }

    @Override
    public void delete(ObjectClass objectClass, Uid uid, OperationOptions options) {

    }
}


