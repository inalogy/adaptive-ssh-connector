package com.inalogy.midpoint.connectors.schema;

import org.identityconnectors.common.logging.Log;

public class SchemaTypeAttribute {
    private static final Log LOG = Log.getLog(UniversalSchemaHandler.class);
    private final boolean required;
    private final String attributeName;
    private final boolean creatable;
    private final boolean updateable;
    private final boolean multivalued;
    private final Class<?> dataType;



    public SchemaTypeAttribute(String attrName, boolean required, boolean creatable, boolean updateable, boolean multivalued, String dataType){
        this.attributeName = attrName;
        this.required = required;
        this.creatable = creatable;
        this.updateable = updateable;
        this.multivalued = multivalued;
        this.dataType = defineDataType(dataType);

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
                throw new IllegalArgumentException("Unsupported data type: " + dataType);
        }
    }
}
