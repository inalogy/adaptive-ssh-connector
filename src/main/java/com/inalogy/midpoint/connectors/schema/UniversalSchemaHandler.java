package com.inalogy.midpoint.connectors.schema;
import com.inalogy.midpoint.connectors.utils.FileHashCalculator;
import org.identityconnectors.common.logging.Log;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class UniversalSchemaHandler {
    private static final Log LOG = Log.getLog(UniversalSchemaHandler.class);
    private final String schemaFilePath;

    public  String getFileSha256() {
        return fileSha256;
    }

    private final String fileSha256;
    private final Set<SchemaType> schemaTypes = new HashSet<>();

    public  Set<SchemaType> getSchemaTypes() {
        return this.schemaTypes;
    }

    public UniversalSchemaHandler(String schemaFilePath) {
        this.schemaFilePath = schemaFilePath;
        this.fileSha256 = FileHashCalculator.calculateSHA256(this.schemaFilePath);
        this.loadSchemaFile();
    }

    private  void loadSchemaFile() {
        try {
            Map<String, Object> schemaFile = readJsonFileAsMap(this.schemaFilePath);
            JsonArray jsonArray = (JsonArray) schemaFile.get("objects");

            for (int j = 0; j < jsonArray.size(); j++) {
                // loop over all objects in json file
                JsonObject jsonObject = jsonArray.getJsonObject(j);
                String name = jsonObject.getString("objectClass");
                String scriptPath = jsonObject.getString("remoteScriptPath");
                JsonArray attributesArray = jsonObject.getJsonArray("attributes");

                ArrayList<String> attributes = new ArrayList<>();
                for (int i = 0; i < attributesArray.size(); i++) {
                    attributes.add(attributesArray.getString(i));
                }
                // create instance of class SchemaType based on json file
                SchemaType schemaType = new SchemaType(name, scriptPath, attributes);
                this.schemaTypes.add(schemaType);
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
