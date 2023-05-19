package com.inalogy.midpoint.connectors.ssh;

import com.inalogy.midpoint.connectors.utils.Constants;
import com.inalogy.midpoint.connectors.cmd.CommandProcessor;
import com.inalogy.midpoint.connectors.cmd.SessionManager;
//import com.inalogy.midpoint.connectors.objects.UniversalFilterTranslator;
import com.inalogy.midpoint.connectors.objects.UniversalObjectsHandler;
import com.inalogy.midpoint.connectors.schema.SchemaType;
import com.inalogy.midpoint.connectors.schema.UniversalSchemaHandler;
import com.inalogy.midpoint.connectors.utils.Constants;
import com.inalogy.midpoint.connectors.utils.FileHashCalculator;
import com.inalogy.midpoint.connectors.utils.SshResponseHandler;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectionFailedException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.PoolableConnector;
import org.identityconnectors.framework.spi.operations.*;

import java.util.*;

@ConnectorClass(displayNameKey = "ssh.connector.display", configurationClass = SshConfiguration.class)
public class SshConnector implements
        PoolableConnector,
        SchemaOp,
        TestOp,
        SearchOp<Filter>,
        CreateOp,
        UpdateDeltaOp,
        DeleteOp
{
    private CommandProcessor commandProcessor;
    private SessionManager sshManager;
    private SshConfiguration configuration;
    private static UniversalSchemaHandler schema = null;
    //Ssh Connector schema cache
    private static final Log LOG = Log.getLog(SshConnector.class);
//    private SessionManager sessionManager;

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void init(Configuration configuration) {
        this.configuration = (SshConfiguration) configuration;
        this.configuration.validate();
        this.sshManager = new SessionManager((SshConfiguration) configuration);
        this.sshManager.connect();
        this.commandProcessor = new CommandProcessor((SshConfiguration) configuration);
    }

    @Override
    public void dispose() {
        schema = null;
        this.configuration = null;
        if (this.sshManager != null) {
            this.sshManager.disconnect();
            this.sshManager = null;
        }
//        commandProcessor = null;

    }

    @Override
    public void test() {
        String testEcho = "echo \"Hello\"";
//        String testEcho = "Get-Process";
        String response = this.sshManager.exec(testEcho).replace("\n", "").replace("\r", "");
        if (!response.equals("Hello")){
            throw new ConnectionFailedException("Error occurred while testing connection");
        }
    }

    @Override
    public Schema schema() {
//        LOG.error("schema method executed");
        SchemaBuilder schemaBuilder = new SchemaBuilder(SshConnector.class);
        String currentFileSha256 = FileHashCalculator.calculateSHA256(this.configuration.getSchemaFilePath());

        if (schema == null){
            schema = new UniversalSchemaHandler(this.configuration.getSchemaFilePath());
            LOG.info("Creating universalSchemaHandler schemaConfigFilePath:" + this.configuration.getSchemaFilePath());

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
    }

    @Override
    public FilterTranslator<Filter> createFilterTranslator(ObjectClass objectClass, OperationOptions options) {
        return new FilterTranslator<Filter>() {
            @Override
            public List<Filter> translate(Filter filter) {
                return Collections.<Filter>emptyList();
            }
        };
    }

    @Override
    public void executeQuery(ObjectClass objectClass, Filter query, ResultsHandler handler, OperationOptions options) {
        LOG.info("executeQuery on {0}, query: {1}, options: {2}", objectClass, query, options);
        try {
//            getSchemaHandler();
            // choosing schema type by key value from map which corresponds to SchemaType object
            SchemaType schemaType = SshConnector.schema.getSchemaTypes().get(objectClass.getObjectClassValue());

            if (schemaType == null) {
                LOG.error("Unsupported ObjectClass");
                throw new IllegalArgumentException("Unsupported ObjectClass: " + objectClass);
            }
            if (query != null){} //TODO
            else {
                String searchScript = schemaType.getSearchScript();
                String sshProcessedCommand = commandProcessor.process(null, searchScript);
                String sshRawResponse = this.sshManager.exec(sshProcessedCommand);
                ArrayList<ConnectorObject> objectsToHandle = new SshResponseHandler(schemaType, sshRawResponse).parseSearchOperation();
                for (ConnectorObject connectorObject: objectsToHandle){
                    handler.handle(connectorObject);
                }
            }
        } catch (Exception e) {
            LOG.error("Error occurred ", e);
            // Handle exception
        }
    }

    @Override
    public Uid create(ObjectClass objectClass, Set<Attribute> createAttributes, OperationOptions options) {
//        getSchemaHandler();
        // choosing schema type by key value from map which corresponds to SchemaType object
        SchemaType schemaType = SshConnector.schema.getSchemaTypes().get(objectClass.getObjectClassValue());
        String createScript = schemaType.getCreateScript();
        String sshProcessedCommand = commandProcessor.process(null, createScript);
        String sshRawResponse = this.sshManager.exec(sshProcessedCommand);
        Uid uid = new SshResponseHandler(schemaType, sshRawResponse).parseCreateOperation();
        return uid;
    }

    @Override
    public Set<AttributeDelta> updateDelta(ObjectClass objectClass, Uid uid, Set<AttributeDelta> modifications, OperationOptions options) {
        return null;
    }

    @Override
    public void delete(ObjectClass objectClass, Uid uid, OperationOptions options) {
        SchemaType schemaType = SshConnector.schema.getSchemaTypes().get(objectClass.getObjectClassValue());
        String deleteScript = schemaType.getDeleteScript();
        String sshProcessedCommand = commandProcessor.process(null, deleteScript);
        String sshRawResponse = this.sshManager.exec(sshProcessedCommand);
        String DeleteResponse = new SshResponseHandler(schemaType, sshRawResponse).parseDeleteOperation();
        if (DeleteResponse == null){
            //success
            return;
        }
        else{
            //return response error
            throw new ConnectorException("Error occurred while deleting user: " + DeleteResponse);
        }
    }

    @Override
    public void checkAlive() {

    }

}


