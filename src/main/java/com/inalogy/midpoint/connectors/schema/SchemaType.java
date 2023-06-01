package com.inalogy.midpoint.connectors.schema;

import java.util.List;

import org.identityconnectors.common.logging.Log;

/**
 * This class represents ObjectClass.
 * <p>
 * The SchemaType class is used to define the structure and
 * characteristics of ObjectClass. Each instance of the
 * SchemaType class represents a distinct type of ObjectClass e.g. user, group
 * <p>
 * @author P-Rovnak
 * @since 1.0
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


    protected SchemaType(String icfsUid, String icfsName, String objectClassName, String createScript, String updateScript, String deleteScript, String searchScript, List<SchemaTypeAttribute> attributes){
        this.icfsUid = icfsUid;
        this.icfsName = icfsName;
        this.objectClassName = objectClassName;
        this.createScript = createScript;
        this.updateScript = updateScript;
        this.deleteScript = deleteScript;
        this.searchScript = searchScript;
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

    public boolean isUidAndNameSame(){
        return (this.icfsName.equals(this.icfsUid));
    }

}
