package com.inalogy.midpoint.connector.ssh.schema;

import org.identityconnectors.common.logging.Log;

/**
 * This class represents an attribute of particular SchemaType.
 *
 * @author P-Rovnak
 * @version 1.0
 * @since 1.0
 */
public class SchemaTypeAttribute {
    private static final Log LOG = Log.getLog(UniversalSchemaHandler.class);
    private final boolean required;
    private final String attributeName;
    private final boolean creatable;
    private final boolean updateable;
    private final boolean multivalued;
    private final Class<?> dataType;
    private final boolean returnedByDefault;


    protected SchemaTypeAttribute(String attrName, boolean required, boolean creatable, boolean updateable, boolean multivalued, String dataType, boolean returnedByDefault){
        this.attributeName = attrName;
        this.required = required;
        this.creatable = creatable;
        this.updateable = updateable;
        this.multivalued = multivalued;
        this.dataType = defineDataType(dataType);
        this.returnedByDefault = returnedByDefault;

    }
    public boolean isRequired() {
        return required;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public boolean isCreatable() {
        return creatable;
    }

    public boolean isUpdateable() {
        return updateable;
    }

    public boolean isMultivalued() {
        return multivalued;
    }

    public Class<?> getDataType() {
        return dataType;
    }

    public boolean isReturnedByDefault(){return returnedByDefault;}
    private Class<?> defineDataType(String dataType){
        switch (dataType.toLowerCase()) {
            case "string":
                return String.class;
            case "boolean":
                return Boolean.class;
            case "int":
                return Integer.class;
            case "bytearray":
                return Byte[].class;
            default:
                LOG.error("Received Unsupported data type from schemaFile: {} Currently supported dataTypes: String, boolean, int, bytearray", dataType);
                throw new IllegalArgumentException("Received Unsupported data type from schemaFile: "+ dataType + "Currently supported dataTypes: String, boolean, int, bytearray");
        }
    }
}
