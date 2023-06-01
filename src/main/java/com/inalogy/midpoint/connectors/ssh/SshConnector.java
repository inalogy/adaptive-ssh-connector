package com.inalogy.midpoint.connectors.ssh;

import java.util.ArrayList;
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
                String searchScript = schemaType.getSearchScript();
                Attribute attribute = AttributeBuilder.build(schemaType.getIcfsUid(), query.byUid);
                queryAttribute.add(attribute);

                String sshProcessedCommand = commandProcessor.process(queryAttribute, searchScript);
                String sshRawResponse = this.sshManager.exec(sshProcessedCommand);
                Set<Map<String, String>> parsedResponse = new SshResponseHandler(schemaType, sshRawResponse).parseSearchOperation();
                Map<String, String> singleLine = parsedResponse.iterator().next(); // search result for single user/object should always return single object
                ConnectorObject connectorObject = UniversalObjectsHandler.convertObjectToConnectorObject(schemaType, singleLine);
                handler.handle(connectorObject);

            }
            else {
                String searchScript = schemaType.getSearchScript();
                String sshProcessedCommand = commandProcessor.process(null, searchScript);
                String sshRawResponse = this.sshManager.exec(sshProcessedCommand);
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
        String createScript = schemaType.getCreateScript();
        String sshProcessedCommand = commandProcessor.process(createAttributes, createScript);
        String sshRawResponse = this.sshManager.exec(sshProcessedCommand);
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
//        UniversalObjectsHandler.handleUpdateDelta(schemaType, uid, modifications, options);
        Set<Attribute> attributeSet = new HashSet<>();
        Attribute icfsAttribute = AttributeBuilder.build(schemaType.getIcfsUid(), uid.getValue());
        attributeSet.add(icfsAttribute);

        for (AttributeDelta attributeDelta: modifications){
            //handle multivalued operations for ADD and REMOVE separately
            if (attributeDelta.getValuesToAdd() != null || attributeDelta.getValuesToRemove() != null){
                Set<Attribute> formattedAttributes = UniversalObjectsHandler.formatMultiValuedAttribute(schemaType, uid, attributeDelta);
                String updateScript = schemaType.getUpdateScript();
                String sshProcessedCommand = commandProcessor.process(formattedAttributes, updateScript);
                String sshRawResponse = this.sshManager.exec(sshProcessedCommand);
                if (sshRawResponse.equals("")){
                    LOG.ok("Successfully Modified object: {0}", schemaType.getObjectClassName());
                }
                else {
                    LOG.error("Error occurred while modifying object " + sshRawResponse);
                    throw new ConnectorException("Error occurred while modifying object " + sshRawResponse);
                }
            } else {
                //handle replace singlevalue
                if (attributeDelta.getValuesToReplace() != null){
                    for (Object value: attributeDelta.getValuesToReplace()){
                        Attribute attribute = AttributeBuilder.build(attributeDelta.getName(), value);
                        attributeSet.add(attribute);
                        String updateScript = schemaType.getUpdateScript();
                        String sshProcessedCommand = commandProcessor.process(attributeSet, updateScript);
                        String sshRawResponse = this.sshManager.exec(sshProcessedCommand);
                        attributeSet.remove(attribute);

                        if (sshRawResponse.equals("")){
                            //update script return "" if it was executed successfully
                            LOG.info("success");
                        }
                        else {
                            throw new ConnectorException("error occurred while modifying object " + sshRawResponse);
                        }
                    }
                }
            }

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
        String deleteScript = schemaType.getDeleteScript();
        Set<Attribute> attributeSet = new HashSet<>();
        Attribute attribute = AttributeBuilder.build(schemaType.getIcfsUid(), uid.getValue());
        attributeSet.add(attribute);
        String sshProcessedCommand = commandProcessor.process(attributeSet, deleteScript);
        String sshRawResponse = this.sshManager.exec(sshProcessedCommand);
        String DeleteResponse = new SshResponseHandler(schemaType, sshRawResponse).parseDeleteOperation();
        if (DeleteResponse == null){
            //success
            LOG.ok("Successfully deleted object: {0} uid: {1}", schemaType.getObjectClassName(), uid.getValue());
            return;
        }
        else {
            //return response error
            LOG.error("Error occurred while deleting object: {0}", sshRawResponse);
            throw new ConnectorException("Error occurred while deleting object: " + DeleteResponse);
        }
    }

    public void getSchemaHandler(){
        if (SshConnector.schema == null){
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
}


