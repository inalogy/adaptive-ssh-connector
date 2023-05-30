package com.inalogy.midpoint.connectors.utils;

import java.util.*;
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

    public Set<Map<String, String>> parseSearchOperation() {
        String[] lines = this.rawResponse.split(Constants.RESPONSE_NEW_LINE_SEPARATOR);
        Set<Map<String, String>> parsedResult = new HashSet<>();
        // read first line that always contains attr names
        String[] attributeNames = lines[0].split(Pattern.quote(Constants.RESPONSE_COLUMN_SEPARATOR));
        for (int i = 1; i <lines.length; i++) {
            String[] attributeValues = lines[i].split(Pattern.quote(Constants.RESPONSE_COLUMN_SEPARATOR));
            Map<String,String> validAttributes = parseAttributes(attributeNames, attributeValues);
            parsedResult.add(validAttributes);

        }
        return parsedResult;
    }

    /**
     * Map valid attributes based on schema
     *  case1:
     *      powershell script returns line[0]:
     *      UniqueID|otherAttribute
     *      this method set icfsUid and icfsName to same value if icfsUid and icfsName in schemaConfig.json are same
     *  case2:
     *      powershell script returns line[0]:
     *      UniqueID|UniqueName|Other Attributes
     *      UniqueID and UniqueName must match icfsUid and icfsName specified in schemaConfig.json
     *      this method set icfsUid and icfsName to corresponding values
     * @param attributeNames  first line returned by ssh which define column names e.g. ExchangeGuid|Attributes
     * @param attributeValues every other line define single object with attributes e.g. 341eb-......|example@mail.com
     * @return parsed attributes map with corresponding pairs e.g. icfsUid="341eb-......", ...
     *
     */
    private Map<String, String> parseAttributes(String[] attributeNames, String[] attributeValues) {
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


    /**
     * @return null if deleteOperation was successful otherwise return error message
     */
    public String parseDeleteOperation() {

        if (this.rawResponse.equals("")){
            return null;
        }
        return this.rawResponse;

    }

    public Uid parseCreateOperation() {
        String[] lines = this.rawResponse.split(Constants.RESPONSE_NEW_LINE_SEPARATOR);

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
}
