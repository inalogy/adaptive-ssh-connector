package com.inalogy.midpoint.connectors.schema;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.inalogy.midpoint.connectors.utils.FileHashCalculator;

import org.identityconnectors.common.logging.Log;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;


/**
 * The UniversalSchemaHandler is responsible for parsing the schemaConfig file
 * and creating instances of SchemaType Objects, which are stored in a Map as instance attribute.
 *
 * @author P-Rovnak
 * @since 1.0
 */
public class UniversalSchemaHandler {
    private static final Log LOG = Log.getLog(UniversalSchemaHandler.class);

    /**
     * Absolute Schema Filepath
     */
    private final String schemaFilePath;
    public  String getFileSha256() {
        return fileSha256;
    }
    private final String fileSha256;
    /**
     * Holds all SchemaTypes in map in which key is: SchemaType.getObjectClassName value: SchemaType Object
     */
    private final Map<String, SchemaType> schemaTypes = new HashMap<>();
    public  Map<String, SchemaType> getSchemaTypes() {
        return this.schemaTypes;
    }

    public UniversalSchemaHandler(String schemaFilePath) {
        this.schemaFilePath = schemaFilePath;
        this.fileSha256 = FileHashCalculator.calculateSHA256(this.schemaFilePath);
        this.loadSchemaFile();
    }

    /**
     * Loads SchemaFile and instantiate SchemaType objects
     * that represents ObjectClasses with all attributes defined in SchemaFile
     */
    private void loadSchemaFile() {
        try {
            Map<String, Object> schemaFile = readJsonFileAsMap(this.schemaFilePath);
            JsonArray jsonArray = (JsonArray) schemaFile.get("objects");

            for (int j = 0; j < jsonArray.size(); j++) {
                // loop over all objects in json file
                JsonObject jsonObject = jsonArray.getJsonObject(j);
                String icfsName = jsonObject.getString("icfsName");
                String icfsUid = jsonObject.getString("icfsUid");
                String objectClass = jsonObject.getString("objectClass");
                String createScript = jsonObject.getString("createScript");
                String updateScript = jsonObject.getString("updateScript");
                String deleteScript = jsonObject.getString("deleteScript");
                String searchScript = jsonObject.getString("searchScript");

                List<SchemaTypeAttribute> attributes = new ArrayList<>();
                try {
                    if (jsonObject.containsKey("attributes")) {
                        //attributes are optional
                        JsonArray attributesArray = jsonObject.getJsonArray("attributes");
                        for (int i = 0; i < attributesArray.size(); i++) {
                            JsonObject attributeObject = attributesArray.getJsonObject(i);
                            for (String attrName : attributeObject.keySet()) {
                                JsonObject attrDetails = attributeObject.getJsonObject(attrName);
                                boolean required = attrDetails.getBoolean("required");
                                boolean creatable = attrDetails.getBoolean("creatable");
                                boolean updateable = attrDetails.getBoolean("updateable");
                                boolean multivalued = attrDetails.getBoolean("multivalued");
                                String dataType = attrDetails.getString("dataType");

                                // Create an instance of class SchemaTypeAttribute based on json file
                                SchemaTypeAttribute attributeType = new SchemaTypeAttribute(attrName, required, creatable, updateable, multivalued, dataType);
                                attributes.add(attributeType);
                            }
                        }
                    }

                    // Create an instance of class SchemaType based on json file
                    SchemaType schemaType = new SchemaType(icfsUid, icfsName, objectClass, createScript, updateScript, deleteScript, searchScript, attributes);
                    this.schemaTypes.put(schemaType.getObjectClassName(), schemaType);

                } catch (NullPointerException e) {
                LOG.error("Null pointer exception occurred in UniversalSchemaHandler: " + e.getMessage());
                throw new RuntimeException("Null pointer exception occurred in UniversalSchemaHandler: " + e.getMessage());
            } catch (ClassCastException e) {
                    LOG.error("Class cast exception occurred in UniversalSchemaHandler: " + e.getMessage());
                    throw new RuntimeException("Class cast exception occurred in UniversalSchemaHandler: " + e.getMessage());
            } catch (Exception e) {
                    LOG.error("An unexpected error occurred: in UniversalSchemaHandler: " + e.getMessage());
                    throw new RuntimeException("An unexpected error occurred: in UniversalSchemaHandler: " + e.getMessage());
            }
            }
        } catch (IOException e) {
            LOG.error("An error occurred while reading SchemaFile: " + e);
            throw new RuntimeException("An error occurred while SchemaFile: " + e);
        }
    }
    public static Map<String, Object> readJsonFileAsMap(String filePath) throws IOException {
        Map<String, Object> resultMap = new HashMap<>();
        try (FileReader fileReader = new FileReader(filePath);
             JsonReader jsonReader = Json.createReader(fileReader)) {
            JsonObject jsonObject = jsonReader.readObject();
            for (String key : jsonObject.keySet()) {
                resultMap.put(key, jsonObject.get(key));
            }
        }
        return resultMap;

    }

}
