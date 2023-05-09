package com.inalogy.midpoint.connectors.schema;
import org.identityconnectors.common.logging.Log;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.util.*;

public class UniversalSchemaHandler {
//TODO need complete rework to load all schema types from single json file
    private static final Log LOG = Log.getLog(UniversalSchemaHandler.class);
    private final String schemaFilePath;
    private String name;
    private  String scriptPath;
    private List<String> attributes;
    public UniversalSchemaHandler(String schemaAbsoluteFilePath){
        this.schemaFilePath = schemaAbsoluteFilePath;
        this.loadSchemaFile();
    }


    private void loadSchemaFile() {
        try {
            Map<String, Object> schemaFile = readJsonFileAsMap(this.schemaFilePath);
            this.name = schemaFile.get("objectClass").toString();
            this.scriptPath = schemaFile.get("remoteScriptPath").toString();
            JsonArray jsonArray = (JsonArray) schemaFile.get("attributes");
            this.attributes = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                this.attributes.add(jsonArray.getString(i));
            }

        } catch (IOException e) {
            LOG.error("An error occurred while reading SchemaFIle: " + e);
            throw new RuntimeException("An error occurred when reading SchemaFIle: " + e);
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

    public String getName() {
        return name;
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public List<String> getAttributes() {
        return attributes;
    }

}
