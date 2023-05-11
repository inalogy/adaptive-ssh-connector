package com.inalogy.midpoint.connectors.objects;

import com.inalogy.midpoint.connectors.schema.SchemaType;
import com.inalogy.midpoint.connectors.schema.SchemaTypeAttribute;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.SchemaBuilder;

public class UniversalObjectsHandler {
    private static final Log LOG = Log.getLog(SchemaType.class);


    public static void buildObjectClass(SchemaBuilder schemaBuilder, SchemaType schemaType){
        ObjectClassInfoBuilder objClassBuilder = new ObjectClassInfoBuilder();
        objClassBuilder.setType(schemaType.getObjectClass()); //TODO setting type by objectClass from schema?
        // Add attributes
        if (schemaType.getAttributes() != null || !schemaType.getAttributes().isEmpty()) {
            //TODO check
            for (SchemaTypeAttribute attribute : schemaType.getAttributes()) {
                AttributeInfoBuilder attrInfoBuilder = new AttributeInfoBuilder(attribute.getAttributeName(), attribute.getDataType());
                attrInfoBuilder.setRequired(attribute.isRequired());
                attrInfoBuilder.setCreateable(attribute.isCreatable());
                attrInfoBuilder.setUpdateable(attribute.isUpdateable());
                attrInfoBuilder.setMultiValued(attribute.isMultivalued());
                objClassBuilder.addAttributeInfo(attrInfoBuilder.build());
            }
        }
        schemaBuilder.defineObjectClass(objClassBuilder.build());
    }
}
