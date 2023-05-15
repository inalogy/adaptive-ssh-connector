package com.inalogy.midpoint.connectors.schema;
import com.inalogy.midpoint.connectors.utils.FileHashCalculator;
import org.identityconnectors.common.logging.Log;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
/**
 * Every SchemaType object is stored inside Map<String, SchemaType>
 */
public class UniversalSchemaHandler {
    private static final Log LOG = Log.getLog(UniversalSchemaHandler.class);
    private final String schemaFilePath;

    public  String getFileSha256() {
        return fileSha256;
    }

    private final String fileSha256;
    private final Map<String, SchemaType> schemaTypes = new HashMap<>();


    // key schemaType.getObjectClass() value: SchemaType object
    public  Map<String, SchemaType> getSchemaTypes() {
        return this.schemaTypes;
    }

    public UniversalSchemaHandler(String schemaFilePath) {
        this.schemaFilePath = schemaFilePath;
        this.fileSha256 = FileHashCalculator.calculateSHA256(this.schemaFilePath);
        this.loadSchemaFile();
    }

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
            }
        } catch (IOException e) {
            LOG.error("An error occurred while reading SchemaFile: " + e);
            throw new RuntimeException("An error occurred when reading SchemaFile: " + e);
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
