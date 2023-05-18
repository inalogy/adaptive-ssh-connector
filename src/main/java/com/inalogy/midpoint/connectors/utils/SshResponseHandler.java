package com.inalogy.midpoint.connectors.utils;

import com.inalogy.midpoint.connectors.schema.SchemaType;
import com.inalogy.midpoint.connectors.schema.SchemaTypeAttribute;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.*;

import java.util.*;
import java.util.regex.Pattern;
import org.identityconnectors.common.logging.Log;

public class SshResponseHandler {
    private static final Log LOG = Log.getLog(SshResponseHandler.class);


    //    private final String responseTypeOperation;
    private final String rawResponse;
    private final SchemaType schemaType;
    private final String operationType;

    public SshResponseHandler(SchemaType schemaType, String operationType, String rawResponse) {
        this.schemaType = schemaType;
        this.operationType = operationType;
        this.rawResponse = rawResponse;
    }

    public ArrayList<ConnectorObject> parseResponse() {
        switch (this.operationType) {
            case Constants.CREATE_OPERATION:
                return parseCreateOperation();
            case Constants.SEARCH_OPERATION:
                return parseSearchOperation();
            case Constants.UPDATE_OPERATION:
                return parseUpdateOperation();
            case Constants.DELETE_OPERATION:
                return parseDeleteOperation();
            default:
                throw new ConnectorException("Unsupported Operation error: " + this.operationType);
        }
    }

    private ArrayList<ConnectorObject> parseSearchOperation() {
        ArrayList<ConnectorObject> connectorObjects = new ArrayList<>();
        String[] lines = this.rawResponse.split("\n");
        String[] attributeNames = lines[0].split(Pattern.quote(Constants.RESPONSE_SEPARATOR)); // read first line that always contains attr names
        for (int i = 1; i <lines.length; i++) {
            String[] attributeValues = lines[i].split(Pattern.quote(Constants.RESPONSE_SEPARATOR));
            Map<String,String> validAttributes = parseAttributes(attributeNames, attributeValues);
            ConnectorObject connectorObject = convertObjectToConnectorObject(validAttributes);
            connectorObjects.add(connectorObject);
//            System.out.println(validAttributes);
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
         *      UniqueID and UniqueName must be equals to icfsUid and icfsName specified in schemaConfig.json
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
                    for (SchemaTypeAttribute sta : this.schemaType.getAttributes()) {
                        if (sta.getAttributeName().equals(attributeName)) {
                            if(attributeValue.equals(Constants.RESPONSE_EMPTY_ATTRIBUTE_SYMBOL)){
                                validAttributes.put(attributeName, "");
                                break;
                            //TODO Validate
                            }
                            validAttributes.put(attributeName, attributeValue);
                            break;
                        }
                    }
                }
            }
        } else {
            //TODO better error handling
            LOG.error("Fatal error: The number of attribute names does not match the number of attribute values");
            throw new ConnectorException("the number of attribute names does not match the number of attribute values.");
        }
        return validAttributes;
    }


    private ArrayList<ConnectorObject> parseUpdateOperation() {
        return null;
    }

    private ArrayList<ConnectorObject> parseDeleteOperation() {
        return null;

    }

    private ArrayList<ConnectorObject> parseCreateOperation() {
        return null;
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
                builder.addAttribute(AttributeBuilder.build(attribute.getKey(), attribute.getValue()));
        }

        return builder.build();
    }



}
