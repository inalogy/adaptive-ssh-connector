package com.inalogy.midpoint.connectors.utils;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

import com.inalogy.midpoint.connectors.schema.SchemaType;
import com.inalogy.midpoint.connectors.schema.SchemaTypeAttribute;
import com.inalogy.midpoint.connectors.utils.dynamicconfig.DynamicConfiguration;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Uid;

/**
 * This class is responsible for handling the response from the SSH server.
 * It parses the raw response into a more manageable format, depending on the operation performed (search, delete, create)
 * It also use SchemaType Object for pairing with response attributes.
 * @author P-Rovnak
 * @since 1.0
 */
public class SshResponseHandler {
    private static final Log LOG = Log.getLog(SshResponseHandler.class);
    private final String rawResponse;
    private final SchemaType schemaType;
    private final DynamicConfiguration dynamicConfiguration;

    /**
     * Constructs a new SshResponseHandler.
     *
     * @param schemaType The schema type associated with the operation.
     * @param rawResponse The raw response returned by the SSH server.
     */
    public SshResponseHandler(SchemaType schemaType, String rawResponse, DynamicConfiguration dynamicConfiguration) {
        this.schemaType = schemaType;
        this.rawResponse = rawResponse;
        this.dynamicConfiguration = dynamicConfiguration;
    }

    /**
     * Parses the raw response from a search operation.
     *
     * @return A set of maps where each map represents an individual result from the search operation. The keys of the map are attribute names and the values are the corresponding attribute values.
     */
    public Set<Map<String, String>> parseSearchOperation() {
        String RESPONSE_NEW_LINE_SEPARATOR = this.dynamicConfiguration.getSettings().getScriptResponseSettings().getResponseNewLineSeparator();
        String RESPONSE_COLUMN_SEPARATOR = this.dynamicConfiguration.getSettings().getScriptResponseSettings().getResponseColumnSeparator();
        String[] lines = this.rawResponse.split(RESPONSE_NEW_LINE_SEPARATOR);
        Set<Map<String, String>> parsedResult = new HashSet<>();
        // read first line that always contains attr names
        String[] attributeNames = lines[0].split(Pattern.quote(RESPONSE_COLUMN_SEPARATOR));
        for (int i = 1; i <lines.length; i++) {
            String[] attributeValues = lines[i].split(Pattern.quote(RESPONSE_COLUMN_SEPARATOR));
            Map<String,String> validAttributes = parseAttributes(attributeNames, attributeValues);
            parsedResult.add(validAttributes);

        }
        return parsedResult;
    }

    /**
     * Parse valid attributes based on schema.
     *
     *  <p>case1:
     *      powershell script returns line[0]:
     *      UniqueID|otherAttribute
     *      this method set icfsUid and icfsName to same value if icfsUid and icfsName in schemaConfig.json are same
     *  <p>case2:
     *      powershell script returns line[0]:
     *      UniqueID|UniqueName|Other Attributes
     *      UniqueID and UniqueName must match icfsUid and icfsName specified in schemaConfig.json
     *      this method set icfsUid and icfsName to corresponding values
     * @param attributeNames  first line returned by Ssh which define column names e.g. ExchangeGuid|Attributes
     * @param attributeValues every other line define single object with attributes e.g. 341eb-......|example@mail.com
     * @return parsed attributes map with corresponding pairs e.g. icfsUid="341eb-......", ...
     *
     */
    private Map<String, String> parseAttributes(String[] attributeNames, String[] attributeValues) {
        String RESPONSE_EMPTY_ATTRIBUTE_SYMBOL = this.dynamicConfiguration.getSettings().getScriptResponseSettings().getScriptEmptyAttribute();
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
                                if (attributeValue.equals(RESPONSE_EMPTY_ATTRIBUTE_SYMBOL)) {
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
                    "Possible cause: Bad script design, empty attributes should be defined as: " + RESPONSE_EMPTY_ATTRIBUTE_SYMBOL);
            throw new ConnectorException("the number of attribute names does not match the number of attribute values.");
        }
        return validAttributes;
    }


    /**
     * @return null if updateOperation or deleteOperation was successful otherwise return error message
     */
    public String HandleUpdateOrDeleteResponse() {
        String UPDATE_SUCCESS_RESPONSE = this.dynamicConfiguration.getSettings().getUpdateOperationSettings().getUpdateSuccessResponse();
        if (this.rawResponse.equals(UPDATE_SUCCESS_RESPONSE)){
            return null;
        }
        // FIXME: make distinction between update and deleteOp
        else if (this.rawResponse.equals(UPDATE_SUCCESS_RESPONSE)){
            return null;
        }
        return this.rawResponse;

    }

    /**
     * Parses the raw response from a create operation.
     *
     * @return The Uid object created as a result of the create operation.
     * The Uid is extracted from the parsed attributes in the raw response.
     */
    public Uid parseCreateOperation() {
        String RESPONSE_COLUMN_SEPARATOR = this.dynamicConfiguration.getSettings().getScriptResponseSettings().getResponseColumnSeparator();
        String[] lines = this.rawResponse.split(this.dynamicConfiguration.getSettings().getScriptResponseSettings().getResponseNewLineSeparator());

        // read first line that always contains attr names
        String[] attributeNames = lines[0].split(Pattern.quote(RESPONSE_COLUMN_SEPARATOR));
        String[] attributeValues = lines[1].split(Pattern.quote(RESPONSE_COLUMN_SEPARATOR));
        Map<String,String> validAttributes = parseAttributes(attributeNames, attributeValues);
        return parseUid(validAttributes);
    }

    /**
     * Parses the Uid from the given map of attributes.
     *
     * @param attributes A map where the key is the attribute name and the value is the attribute value.
     * It expects the map to contain either "icfsUid" or "icfsName" as a key.
     * @return The Uid object corresponding to the Uid value found in the map.
     * @throws ConnectorException If neither "icfsUid" nor "icfsName" is found in the attributes map.
     */
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
