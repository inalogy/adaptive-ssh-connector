package com.inalogy.midpoint.connectors.ssh;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.inalogy.midpoint.connectors.filter.SshFilter;
import com.inalogy.midpoint.connectors.filter.SshFilterTranslator;
import com.inalogy.midpoint.connectors.utils.Constants;
import com.inalogy.midpoint.connectors.cmd.CommandProcessor;
import com.inalogy.midpoint.connectors.cmd.SessionManager;
import com.inalogy.midpoint.connectors.objects.UniversalObjectsHandler;
import com.inalogy.midpoint.connectors.schema.SchemaType;
import com.inalogy.midpoint.connectors.schema.UniversalSchemaHandler;
import com.inalogy.midpoint.connectors.utils.FileHashCalculator;
import com.inalogy.midpoint.connectors.utils.SshResponseHandler;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectionFailedException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.PoolableConnector;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateDeltaOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;


@ConnectorClass(displayNameKey = "ssh.connector.display", configurationClass = SshConfiguration.class)
public class SshConnector implements
        PoolableConnector,
        SchemaOp,
        TestOp,
        SearchOp<SshFilter>,
        CreateOp,
        UpdateDeltaOp,
        DeleteOp
{
    private CommandProcessor commandProcessor;
    private SessionManager sshManager;
    private SshConfiguration configuration;
    //Ssh Connector schema cache
    private static UniversalSchemaHandler schema = null;
    private static final Log LOG = Log.getLog(SshConnector.class);

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void init(Configuration configuration) {
        this.configuration = (SshConfiguration) configuration;
        this.configuration.validate();
        this.sshManager = new SessionManager((SshConfiguration) configuration);
        this.sshManager.initSshClient();
        this.commandProcessor = new CommandProcessor((SshConfiguration) configuration);
    }

    @Override
    public void dispose() {
        this.configuration = null;
        if (SshConnector.schema != null) {
            SshConnector.schema = null;
        }
        if (this.commandProcessor != null) {
            this.commandProcessor = null;
        }
        if (this.sshManager != null) {
            this.sshManager.disposeSshClient();
            this.sshManager = null;
        }
    }

    /**
     * Simple test connection with echo command.
     */
    @Override
    public void test() {
        String testEcho = "echo \"Hello\"";
        String response = this.sshManager.exec(testEcho).replace("\n", "").replace("\r", "");
        if (!response.equals("Hello")){
            LOG.error("Error occurred in test() method while testing Ssh connection");
            throw new ConnectionFailedException("Error occurred in test() method while testing Ssh connection");
        }
    }

    /**
     * Schema is cached after first initialisation, and is automatically refreshed whenever hash of schema File is changed.
     */
    @Override
    public Schema schema() {
        SchemaBuilder schemaBuilder = new SchemaBuilder(SshConnector.class);
        String currentFileSha256 = FileHashCalculator.calculateSHA256(this.configuration.getSchemaFilePath());

        if (SshConnector.schema == null){
            SshConnector.schema = new UniversalSchemaHandler(this.configuration.getSchemaFilePath());
            LOG.ok("Creating universalSchemaHandler schemaConfigFilePath:" + this.configuration.getSchemaFilePath());

        }
        else if (schema != null && currentFileSha256 != null && !schema.getFileSha256().equals(currentFileSha256)){
            // if sha256 of schemaFile is unchanged we don't need to fetch it again
            LOG.ok("Change in schemaConfigFile detected");
            SshConnector.schema = new UniversalSchemaHandler(this.configuration.getSchemaFilePath());
        }
        for (SchemaType schemaType: SshConnector.schema.getSchemaTypes().values()){
            UniversalObjectsHandler.buildObjectClass(schemaBuilder, schemaType);
        }
        return schemaBuilder.build();
    }

    @Override
    public FilterTranslator<SshFilter> createFilterTranslator(ObjectClass objectClass, OperationOptions options) {
        return new SshFilterTranslator();
    }

    /**
     * executeQuery execute searchScript on remote ssh server.
     * if query contains SshFilter.byUid it create query with appropriate flag that match schema definition and returns exact object that match query Uid.
     * Otherwise, executeQuery returns all output/objects from searchScript, paging is not supported yet.
     */
    @Override
    public void executeQuery(ObjectClass objectClass, SshFilter query, ResultsHandler handler, OperationOptions options) {
        LOG.info("executeQuery on {0}, query: {1}, options: {2}", objectClass, query, options);
        getSchemaHandler();
        try {
            // choosing schema type by key value from map which corresponds to SchemaType object
            SchemaType schemaType = SshConnector.schema.getSchemaTypes().get(objectClass.getObjectClassValue());

            if (schemaType == null) {
                LOG.error("Unsupported ObjectClass: " + objectClass);
                throw new IllegalArgumentException("Unsupported ObjectClass: " + objectClass);
            }
            if (query != null && query.byUid != null){
                //build single object query byUid and create corresponding shell command
                Set<Attribute> queryAttribute = new HashSet<>();
                Attribute attribute = AttributeBuilder.build(schemaType.getIcfsUid(), query.byUid);
                queryAttribute.add(attribute);

                String sshRawResponse = processAndExecuteCommand(queryAttribute, Constants.SEARCH_OPERATION, schemaType);
                Set<Map<String, String>> parsedResponse = new SshResponseHandler(schemaType, sshRawResponse).parseSearchOperation();
                Map<String, String> singleLine = parsedResponse.iterator().next(); // search result for single user/object should always return single object
                ConnectorObject connectorObject = UniversalObjectsHandler.convertObjectToConnectorObject(schemaType, singleLine);
                handler.handle(connectorObject);

            }
            else {
                String sshRawResponse = processAndExecuteCommand(null, Constants.SEARCH_OPERATION, schemaType);
                Set<Map<String, String>> parsedResponse  = new SshResponseHandler(schemaType, sshRawResponse).parseSearchOperation();
                for (Map<String, String> parsedResponseLine: parsedResponse){
                    ConnectorObject connectorObject = UniversalObjectsHandler.convertObjectToConnectorObject(schemaType, parsedResponseLine);
                    handler.handle(connectorObject);
                }
            }
        } catch (Exception e) {
            LOG.error("Error occurred while executing query: ", e);
        }
    }

    /**
     * create Method execute createScript through ssh on target system.
     * Set Uid based on response returned from the script.
     */
    @Override
    public Uid create(ObjectClass objectClass, Set<Attribute> createAttributes, OperationOptions options) {
        getSchemaHandler();
        SchemaType schemaType = SshConnector.schema.getSchemaTypes().get(objectClass.getObjectClassValue());
        String sshRawResponse = processAndExecuteCommand(createAttributes, Constants.CREATE_OPERATION, schemaType);
        Uid uid = new SshResponseHandler(schemaType, sshRawResponse).parseCreateOperation();
        return uid;
        //TODO add error handling if uid||name already exists
    }


    /**
     * Process modifications sent from midpoint and transform them based on modification type.
     * Single value is processed without much overhead, however multivalued attribute need to be formatted in a way that
     * remote script understand which attribute add and which to remove
     * for that we use {@link Constants#MICROSOFT_EXCHANGE_ADD_UPDATEDELTA} and {@link Constants#MICROSOFT_EXCHANGE_REMOVE_UPDATEDELTA}
     *
     */
    @Override
    public Set<AttributeDelta> updateDelta(ObjectClass objectClass, Uid uid, Set<AttributeDelta> modifications, OperationOptions options) {
        LOG.ok("objectClass : {0} uid: {1} modifications: {2} operationOptions: {3}", objectClass, uid.getValue(), modifications, options);
        getSchemaHandler();
        SchemaType schemaType = SshConnector.schema.getSchemaTypes().get(objectClass.getObjectClassValue());
        Set<Attribute> attributeSet = new HashSet<>();
        Attribute icfsAttribute = AttributeBuilder.build(schemaType.getIcfsUid(), uid.getValue());
        attributeSet.add(icfsAttribute);

        for (AttributeDelta attributeDelta: modifications){
            //handle multivalued operations for ADD and REMOVE separately
            if (attributeDelta.getValuesToAdd() != null || attributeDelta.getValuesToRemove() != null){
                Set<Attribute> formattedAttributes = UniversalObjectsHandler.formatMultiValuedAttribute(attributeDelta);
                attributeSet.addAll(formattedAttributes);

            } else {
                //handle replace singlevalue
                if (attributeDelta.getValuesToReplace() != null){
                    for (Object value: attributeDelta.getValuesToReplace()){
                        Attribute attribute = AttributeBuilder.build(attributeDelta.getName(), value);
                        attributeSet.add(attribute);
                    }
                }
            }

        }

        String sshRawResponse = processAndExecuteCommand(attributeSet, Constants.UPDATE_OPERATION, schemaType);
        String response = new SshResponseHandler(schemaType, sshRawResponse).HandleUpdateOrDeleteResponse();
        if (response != Constants.MICROSOFT_EXCHANGE_RESPONSE_SUCCESS_SYMBOL){
            LOG.error("Error occurred while updating entity: {0}", sshRawResponse);
            throw new RuntimeException("Error occurred while updating entity: " + sshRawResponse);
        }
        return null;
    }


    /**
     * delete method, execute script on target system with appropriate flag.
     * Script should return "" if operation was successful
     * @throws ConnectorException if remote script returns any other output
     */
    @Override
    public void delete(ObjectClass objectClass, Uid uid, OperationOptions options) {
        getSchemaHandler();
        SchemaType schemaType = SshConnector.schema.getSchemaTypes().get(objectClass.getObjectClassValue());
        Set<Attribute> attributeSet = new HashSet<>();
        Attribute attribute = AttributeBuilder.build(schemaType.getIcfsUid(), uid.getValue());
        attributeSet.add(attribute);
        String sshRawResponse = processAndExecuteCommand(attributeSet, Constants.DELETE_OPERATION, schemaType);
        String deleteResponse = new SshResponseHandler(schemaType, sshRawResponse).HandleUpdateOrDeleteResponse();
        if (deleteResponse != Constants.MICROSOFT_EXCHANGE_RESPONSE_SUCCESS_SYMBOL){
            LOG.error("Error occured while deleting entity: {0}", deleteResponse);
            throw new RuntimeException("Error occured while deleting entity: " + deleteResponse);
        }
    }

    public void getSchemaHandler(){
        if (SshConnector.schema == null){
            schema();

        } else if (!SshConnector.schema.getFileSha256().equals(FileHashCalculator.calculateSHA256(this.configuration.getSchemaFilePath()))) {
            LOG.warn("Change in schemaFile detected");
            schema();
        }
    }

    /**
     * Checks if SshClient is connected if not poolable Connector dispose this instance
     */
    @Override
    public void checkAlive() {
        boolean isAlive = this.sshManager.isConnectionAlive();
        if (!isAlive){
            LOG.info("connector is not alive");
            throw new ConnectionFailedException();
        }
    }
    public String processAndExecuteCommand(Set<Attribute> attributes, String operationName, SchemaType schemaType){
        String operationScript;
        switch (operationName){
            case Constants.SEARCH_OPERATION:
                operationScript = schemaType.getSearchScript();
                break;
            case Constants.UPDATE_OPERATION:
                operationScript = schemaType.getUpdateScript();
                break;
            case Constants.CREATE_OPERATION:
                operationScript = schemaType.getCreateScript();
                break;
            case Constants.DELETE_OPERATION:
                operationScript = schemaType.getDeleteScript();
                break;
            default:
                LOG.error("Unsupported operation");
                throw new RuntimeException("Unsupported operation");
        }
        String sshProcessedCommand = commandProcessor.process(attributes, operationScript);
        return this.sshManager.exec(sshProcessedCommand);
    }
}

