package com.inalogy.midpoint.connectors.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.inalogy.midpoint.connectors.schema.SchemaType;
import com.inalogy.midpoint.connectors.schema.SchemaTypeAttribute;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.AttributeBuilder;

public class SshResponseHandler {
    private static final Log LOG = Log.getLog(SshResponseHandler.class);
    private final String rawResponse;
    private final SchemaType schemaType;

    public SshResponseHandler(SchemaType schemaType, String rawResponse) {
        this.schemaType = schemaType;
        this.rawResponse = rawResponse;
    }

    public ArrayList<ConnectorObject> parseSearchOperation() {
        ArrayList<ConnectorObject> connectorObjects = new ArrayList<>();
        String[] lines = this.rawResponse.split(Constants.RESPONSE_NEW_LINE_SEPARATOR);

        // read first line that always contains attr names
        String[] attributeNames = lines[0].split(Pattern.quote(Constants.RESPONSE_COLUMN_SEPARATOR));
        for (int i = 1; i <lines.length; i++) {
            String[] attributeValues = lines[i].split(Pattern.quote(Constants.RESPONSE_COLUMN_SEPARATOR));
            Map<String,String> validAttributes = parseAttributes(attributeNames, attributeValues);
            ConnectorObject connectorObject = convertObjectToConnectorObject(validAttributes);
            connectorObjects.add(connectorObject);
        }
        return connectorObjects;
    }

    private Map<String, String> parseAttributes(String[] attributeNames, String[] attributeValues) {
        /** this method map valid attributes based on schema
         *  case1:
         *      powershell script returns line[0]:
         *      UniqueID|otherAttribute
         *      this method set icfsUid and icfsName to same value if icfsUid and icfsName in schemaConfig.json are same
         * <p>
         *  case2: powershell script returns line[0]:
         *      UniqueID|UniqueName|Other Attributes
         *      UniqueID and UniqueName must match icfsUid and icfsName specified in schemaConfig.json
         *      this method set icfsUid and icfsName to corresponding values
         **/
        Map<String, String> validAttributes = new HashMap<>();
        if (attributeNames.length == attributeValues.length) {
            for (int i = 0; i < attributeNames.length; i++) {
                String attributeName = attributeNames[i];
                String attributeValue = attributeValues[i];
                if (attributeName.equals(this.schemaType.getIcfsName())) {
                    validAttributes.put("icfsName", attributeValue);
                    if (this.schemaType.isUidAndNameSame()) {
                        validAttributes.put("icfsUid", attributeValue);
                    }

                } else if (attributeName.equals(this.schemaType.getIcfsUid())) {
                    validAttributes.put("icfsUid", attributeValue);
                } else {
                    // process all other attributes that match schema
                    if(schemaType.getAttributes() !=null) {
                        for (SchemaTypeAttribute sta : this.schemaType.getAttributes()) {
                            if (sta.getAttributeName().equals(attributeName)) {
                                if (attributeValue.equals(Constants.RESPONSE_EMPTY_ATTRIBUTE_SYMBOL)) {
                                    validAttributes.put(attributeName, "");
                                    break;
                                }
                                validAttributes.put(attributeName, attributeValue);
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            LOG.error("Fatal error: The number of attribute names does not match the number of attribute values " +
                    "Possible cause: Bad script design, empty attributes should be defined as: " + Constants.RESPONSE_EMPTY_ATTRIBUTE_SYMBOL);
            throw new ConnectorException("the number of attribute names does not match the number of attribute values.");
        }
        return validAttributes;
    }



    public String parseDeleteOperation() {

        if (this.rawResponse.equals("")){
            return null;
        }
        return this.rawResponse;

    }

    public Uid parseCreateOperation() {
        String[] lines = this.rawResponse.split("\n");

        // read first line that always contains attr names
        String[] attributeNames = lines[0].split(Pattern.quote(Constants.RESPONSE_COLUMN_SEPARATOR));
        String[] attributeValues = lines[1].split(Pattern.quote(Constants.RESPONSE_COLUMN_SEPARATOR));
        Map<String,String> validAttributes = parseAttributes(attributeNames, attributeValues);
        return parseUid(validAttributes);
    }

    private Uid parseUid(Map<String, String> attributes) {
        String uidValue = attributes.get("icfsUid");
        String nameValue = attributes.get("icfsName");

        if(uidValue != null){
            return new Uid(uidValue);
        }
        else if(nameValue != null){
            return new Uid(nameValue);
        }
        else {
            LOG.error("Fatal Error: Cannot find: " + this.schemaType.getIcfsUid() + " " + this.schemaType.getIcfsName());
            throw new ConnectorException("Fatal Error: Cannot find: " + this.schemaType.getIcfsUid() + " " + this.schemaType.getIcfsName());
        }
    }
    private ConnectorObject convertObjectToConnectorObject(Map<String, String> attributes) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        ObjectClass objectClass = new ObjectClass(this.schemaType.getObjectClassName());
        builder.setObjectClass(objectClass);

        builder.setUid(new Uid(attributes.get("icfsUid")));
        builder.setName(new Name(attributes.get("icfsName")));
        attributes.remove("icfsUid");
        attributes.remove("icfsName");

        for (Map.Entry<String, String> attribute : attributes.entrySet()) {

            // check if attribute is multivalued based on schema
            Optional<Boolean> multivaluedAttribute = schemaType.getAttributes().stream()
                    .filter(attr -> attr.getAttributeName().equals(attribute.getKey()))
                    .map(SchemaTypeAttribute::isMultivalued)
                    .findFirst();
            if (multivaluedAttribute.isPresent()) {
                LOG.ok("converting multivalued attribute " + attribute.getKey());
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
