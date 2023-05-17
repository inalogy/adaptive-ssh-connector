package com.inalogy.midpoint.connectors.objects;

import com.inalogy.midpoint.connectors.schema.SchemaType;
import com.inalogy.midpoint.connectors.schema.SchemaTypeAttribute;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.*;

public class UniversalObjectsHandler {
    private static final Log LOG = Log.getLog(SchemaType.class);


    public static void buildObjectClass(SchemaBuilder schemaBuilder, SchemaType schemaType){
        ObjectClassInfoBuilder objClassBuilder = new ObjectClassInfoBuilder();
        objClassBuilder.setType(schemaType.getObjectClassName()); //TODO setting type by objectClass from schema?
        // Add attributes
        String icfsName = schemaType.getIcfsName();
        String icfsUid = schemaType.getIcfsUid();

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
        if (icfsName != null) {
            // TODO if null break?
            AttributeInfoBuilder nameAttrBuilder = new AttributeInfoBuilder(Name.NAME, String.class);
            nameAttrBuilder.setRequired(true);
            nameAttrBuilder.setCreateable(true);
            nameAttrBuilder.setUpdateable(true);
            nameAttrBuilder.setReadable(true);
            objClassBuilder.addAttributeInfo(nameAttrBuilder.build());
        }
        if (icfsUid != null) {
            AttributeInfoBuilder uidAttrBuilder = new AttributeInfoBuilder(Uid.NAME, String.class);
            uidAttrBuilder.setRequired(true);
            uidAttrBuilder.setCreateable(true);
            uidAttrBuilder.setUpdateable(true);
            uidAttrBuilder.setReadable(true);
            objClassBuilder.addAttributeInfo(uidAttrBuilder.build());
        }
        schemaBuilder.defineObjectClass(objClassBuilder.build());
    }

}
