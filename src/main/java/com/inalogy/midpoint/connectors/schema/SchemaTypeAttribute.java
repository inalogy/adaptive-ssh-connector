package com.inalogy.midpoint.connectors.schema;

public class SchemaTypeAttribute {
    private final boolean required;
    private final String attributeName;
    private final boolean creatable;
    private final boolean updateable;
    private final boolean multivalued;
    private final String dataType;



    public SchemaTypeAttribute(String attrName, boolean required, boolean creatable, boolean updateable, boolean multivalued, String dataType){
        this.attributeName = attrName;
        this.required = required;
        this.creatable = creatable;
        this.updateable = updateable;
        this.multivalued = multivalued;
        this.dataType = dataType;

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

    public String getDataType() {
        return dataType;
    }
}
