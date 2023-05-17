package com.inalogy.midpoint.connectors.utils;

import com.inalogy.midpoint.connectors.schema.SchemaType;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.*;

import java.util.ArrayList;

public class SshResponseHandler {

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

        for (String line : lines) {
            // Split each line by "|"
            String[] parts = line.split("\\|");
            String smtpMail = parts[0];
            String mailboxName = parts[1];

            System.out.println("SMTP Mail: " + smtpMail);
            System.out.println("Mailbox Name: " + mailboxName);
            ConnectorObject connectorObject = convertObjectToConnectorObject(smtpMail, mailboxName);
            connectorObjects.add(connectorObject);
        }
        return connectorObjects;
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

    private ConnectorObject convertObjectToConnectorObject(String smtpMail, String mailboxName) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        ObjectClass objectClass = new ObjectClass(this.schemaType.getObjectClassName());
        builder.setObjectClass(objectClass);
        builder.setUid(new Uid(smtpMail));
        builder.setName(new Name(smtpMail));
        builder.setUid(smtpMail);
//        builder.addAttribute(AttributeBuilder.build("smtpMail", smtpMail));
        builder.addAttribute(AttributeBuilder.build("mailboxName", mailboxName));

        return builder.build();
    }
}
