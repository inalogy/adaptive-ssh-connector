package com.inalogy.midpoint.connectors.schema;

import org.identityconnectors.common.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * each schemaType is instance of this class
 */
public class SchemaType {
    private static final Log LOG = Log.getLog(SchemaType.class);

    private final String name;
    private final String createScript;
    private final String updateScript;


    private final String searchScript;

    private final String deleteScript;

    private final List<SchemaTypeAttribute> attributes;
    private final String objectClass;
    private final String id;


    public SchemaType( String id, String name, String objectClass, String createScript, String updateScript, String searchScript, String deleteScript, List<SchemaTypeAttribute> attributes){
        this.name = name;
        this.id = id;
        this.objectClass = objectClass;
        this.createScript = createScript;
        this.updateScript = updateScript;
        this.searchScript = searchScript;
        this.deleteScript = deleteScript;
        this.attributes = attributes;

    }

    public String getName() {
        return name;
    }

    public String getCreateScript() {
        return createScript;
    }

    public String getUpdateScript() {
        return updateScript;
    }

    public String getSearchScript() {
        return searchScript;
    }

    public String getDeleteScript() {
        return deleteScript;
    }

    public String getObjectClass() {
        return objectClass;
    }

    public String getId() {
        return id;
    }


    public List<SchemaTypeAttribute> getAttributes() {
        return attributes;
    }

}
