package com.inalogy.midpoint.connectors.objects;

import com.inalogy.midpoint.connectors.schema.SchemaType;
import com.inalogy.midpoint.connectors.schema.SchemaTypeAttribute;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.Name;


public class UniversalObjectsHandler {
    private static final Log LOG = Log.getLog(SchemaType.class);

    /**
     * Builds a specific ObjectClass
     * and adds it to the given SchemaBuilder
     * based on the provided SchemaType.
     * This method helps in creating the object class dynamically based on the SchemaType.
     * It will add the created object class to the schema builder.
     *
     * @param schemaBuilder  the SchemaBuilder instance to which the built object class is to be added.
     * @param schemaType     the SchemaType that is used to build the object class.
     */
    public static void buildObjectClass(SchemaBuilder schemaBuilder, SchemaType schemaType){
        ObjectClassInfoBuilder objClassBuilder = new ObjectClassInfoBuilder();
        objClassBuilder.setType(schemaType.getObjectClassName());
        String icfsName = schemaType.getIcfsName();
        String icfsUid = schemaType.getIcfsUid();

        if (schemaType.getAttributes() != null || !schemaType.getAttributes().isEmpty()) {
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
            AttributeInfoBuilder nameAttrBuilder = new AttributeInfoBuilder(Name.NAME, String.class);
            nameAttrBuilder.setRequired(true);
            nameAttrBuilder.setCreateable(true);
            nameAttrBuilder.setUpdateable(false);
            nameAttrBuilder.setReadable(true);
            objClassBuilder.addAttributeInfo(nameAttrBuilder.build());
        }
        if (icfsUid != null) {
            AttributeInfoBuilder uidAttrBuilder = new AttributeInfoBuilder(Uid.NAME, String.class);
            uidAttrBuilder.setRequired(true);
            uidAttrBuilder.setCreateable(true);
            uidAttrBuilder.setUpdateable(false);
            uidAttrBuilder.setReadable(true);
            objClassBuilder.addAttributeInfo(uidAttrBuilder.build());
        }
        LOG.ok("buildingObjectClass for: " + schemaType.getObjectClassName());
        schemaBuilder.defineObjectClass(objClassBuilder.build());
    }

}
