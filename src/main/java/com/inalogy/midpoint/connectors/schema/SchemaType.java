package com.inalogy.midpoint.connectors.schema;

import org.identityconnectors.common.logging.Log;

import java.util.List;

/**
 * each schemaType is instance of this class
 */
public class SchemaType {
    private static final Log LOG = Log.getLog(SchemaType.class);

    private final String icfsName;
    private final String createScript;
    private final String updateScript;


    private final String searchScript;

    private final String deleteScript;

    private final List<SchemaTypeAttribute> attributes;
    private final String objectClassName;
    private final String icfsUid;


    public SchemaType(String icfsUid, String icfsName, String objectClassName, String createScript, String updateScript, String searchScript, String deleteScript, List<SchemaTypeAttribute> attributes){
        this.icfsUid = icfsUid;
        this.icfsName = icfsName;
        this.objectClassName = objectClassName;
        this.createScript = createScript;
        this.updateScript = updateScript;
        this.searchScript = searchScript;
        this.deleteScript = deleteScript;
        this.attributes = attributes;

    }

    public String getIcfsName() {
        return icfsName;
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

    public String getObjectClassName() {
        return objectClassName;
    }

    public String getIcfsUid() {
        return icfsUid;
    }


    public List<SchemaTypeAttribute> getAttributes() {
        return attributes;
    }

}
