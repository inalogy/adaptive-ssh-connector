package com.inalogy.midpoint.connectors.objects;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.inalogy.midpoint.connectors.schema.SchemaType;
import com.inalogy.midpoint.connectors.schema.SchemaTypeAttribute;
import com.inalogy.midpoint.connectors.utils.Constants;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.AttributeBuilder;

public class ObjectConverter {
    private static final Log LOG = Log.getLog(ObjectConverter.class);


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
}
