package com.inalogy.midpoint.connectors.utils;

import com.inalogy.midpoint.connectors.schema.SchemaType;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;

public class SshResponseHandler {

//    private final String responseTypeOperation;
    private final String rawResponse;
    private final SchemaType schemaType;
    private final String operationType;

    public SshResponseHandler(SchemaType schemaType,String operationType, String rawResponse){
        this.schemaType = schemaType;
        this.operationType  = operationType;
        this.rawResponse = rawResponse;
    }

    public String parseResponse(){
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

    private String parseSearchOperation() {
        String[] lines = this.rawResponse.split("\\r?\\n");
        for (int i = 2; i < lines.length; i++) {
            String line = lines[i];

            String[] columns = line.split("\\s+", 3); //firstName lastname email
            System.out.println("firstName: " + columns[0] + " lastName: " + columns[1] + " email: " + columns[2]);
        }
        return null;
    }


    private String parseUpdateOperation(){
        return null;
    }
    private String parseDeleteOperation(){
        return null;

    }
    private String parseCreateOperation(){
        return null;
    }

    private ConnectorObject convertObjectToConnectorObject(){
        ConnectorObjectBuilder object = new ConnectorObjectBuilder();

        return null;
    }
}
