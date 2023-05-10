package com.inalogy.midpoint.connectors.schema;

import org.identityconnectors.common.logging.Log;

import java.util.List;

/**
 * each schemaType is instance of this class
 */
public class SchemaType {
    private static final Log LOG = Log.getLog(SchemaType.class);

    private final String name;
    private final String scriptPath;
    private final List<String> attributes;

    public SchemaType(String name, String scriptPath, List<String> attributes){
        this.name = name;
        this.scriptPath = scriptPath;
        this.attributes = attributes;

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
