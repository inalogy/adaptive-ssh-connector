package com.inalogy.midpoint.connectors.objects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.inalogy.midpoint.connectors.schema.SchemaType;
import com.inalogy.midpoint.connectors.schema.SchemaTypeAttribute;
import com.inalogy.midpoint.connectors.utils.Constants;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeDelta;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.Name;

/**
 * A utility class responsible for the transformation and handling of object classes and connector objects.
 * <p>
 * This class provides functionality for building an {@link ObjectClass} instance based on a provided
 * {@link SchemaType}, and for converting a Map of attributes into a {@link ConnectorObject}.
 * </p>
 * @author P-Rovnak
 * @since 1.0
 */
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

    /**
     * Converts a given Map of attributes to a ConnectorObject.
     * <p>
     * The conversion process is influenced by the provided SchemaType, which defines how
     * the attributes should be mapped into the ConnectorObject.
     * </p>
     * @param schemaType SchemaType object representing currently processed object e.g. user,group
     * @param attributes Map of strings representing currently processed object
     * @return ConnectorObject
     */
    public static ConnectorObject convertObjectToConnectorObject(SchemaType schemaType, Map<String, String> attributes) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        ObjectClass objectClass = new ObjectClass(schemaType.getObjectClassName());
        builder.setObjectClass(objectClass);

        builder.setUid(new Uid(attributes.get("icfsUid")));
        builder.setName(new Name(attributes.get("icfsName")));
        attributes.remove("icfsUid");
        attributes.remove("icfsName");

        for (Map.Entry<String, String> attribute : attributes.entrySet()) {

            // check if attribute is multivalued based on schema
            boolean multivaluedAttribute = schemaType.getAttributes().stream()
                    .filter(attr -> attr.getAttributeName().equals(attribute.getKey()))
                    .map(SchemaTypeAttribute::isMultivalued)
                    .findFirst()
                    .orElse(false);
            if (multivaluedAttribute) {
                LOG.ok("converting multivalued attribute {0}", attribute.getKey());
                String[] values = attribute.getValue().split(Pattern.quote(Constants.MICROSOFT_EXCHANGE_RESPONSE_MULTIVALUED_SEPARATOR));
                builder.addAttribute(AttributeBuilder.build(attribute.getKey(), values));
            } else {
                // Attribute is not multiValued. Add it as a single value.
                builder.addAttribute(AttributeBuilder.build(attribute.getKey(), attribute.getValue()));
            }
        }
        return builder.build();
    }
    /**
     * Process the provided attributeDelta to prepare for an SSH request.
     * Extracts all values from the attributeDelta, formats them for compatibility with
     * the remote script using {@link Constants#MICROSOFT_EXCHANGE_ADD_UPDATEDELTA} or {@link Constants#MICROSOFT_EXCHANGE_REMOVE_UPDATEDELTA},
     * and adds them to ArrayList, finally, it executes SSH request.
     * @param attributeDelta of currently processed modification
     */
    public static Set<Attribute> formatMultiValuedAttribute(AttributeDelta attributeDelta){
        Set<Attribute> attributeSet = new HashSet<>();
        ArrayList<String> multivaluedAttributes = new ArrayList<>();

        if (attributeDelta.getValuesToAdd() != null) {
            for (Object value : attributeDelta.getValuesToAdd()) {
                String attributeValue = Constants.MICROSOFT_EXCHANGE_ADD_UPDATEDELTA + value;
                multivaluedAttributes.add(attributeValue);
            }
        }
        if (attributeDelta.getValuesToRemove() != null) {
            for (Object value : attributeDelta.getValuesToRemove()) {
                String attributeValue =  Constants.MICROSOFT_EXCHANGE_REMOVE_UPDATEDELTA + value;
                multivaluedAttributes.add(attributeValue);
            }
        }

        Attribute multivaluedAttribute = AttributeBuilder.build(attributeDelta.getName(), multivaluedAttributes);
        attributeSet.add(multivaluedAttribute);

        return  attributeSet;


    }
}
