package com.inalogy.midpoint.connectors.utils;

/**
 * A class that defines constant values used throughout the project.
 * @author P-Rovnak
 * @since 1.0
 */
public class Constants {
    public static final String TYPE_POWERSHELL = "powershell";
    public static final String TYPE_SHELL = "shell";
    public static final String SEARCH_OPERATION = "searchOperation";
    public static final String UPDATE_OPERATION = "updateOperation";
    public static final String CREATE_OPERATION = "createOperation";
    public static final String DELETE_OPERATION = "deleteOperation";
    /**
     * Separator that should be returned by remote Script. It should never match RESPONSE_NEW_LINE_SEPARATOR
     */
    public static final String RESPONSE_COLUMN_SEPARATOR = "|";
    /**
     * Each line represent single object for processing, e.g. user,group.
     */
    public static final String RESPONSE_NEW_LINE_SEPARATOR = "\n";

    /**
     * remote script is supposed to return this Constant that represent empty attribute
     */
    public static final String RESPONSE_EMPTY_ATTRIBUTE_SYMBOL = "null";

    public static final String SPECIAL_CONNID_NAME = "__NAME__";
    public static final String SPECIAL_CONNID_UID = "__UID__";
    public static final String SPECIAL_CONNID_PASSWORD = "__PASSWORD__";

    /**
     * Value of this constant depends on the implementation of remote script.
     */
    public static final String MICROSOFT_EXCHANGE_NAME_FLAG = "name";
    public static final String MICROSOFT_EXCHANGE_EMAIL_FLAG = "email";
    public static final String MICROSOFT_EXCHANGE_PASSWORD_FLAG = "password";

    /**
     * By default, Microsoft Exchange returns multivalued attribute separated by " "
     */
    public static final String MICROSOFT_EXCHANGE_RESPONSE_MULTIVALUED_SEPARATOR = " ";
    public static final String MICROSOFT_EXCHANGE_ADD_UPDATEDELTA = "ADD:";
    public static final String MICROSOFT_EXCHANGE_REMOVE_UPDATEDELTA = "REMOVE:";

    /**
     * This constant should be returned by remote script if updateDelta or Delete() operation were successfully executed
     */
    public static final String MICROSOFT_EXCHANGE_RESPONSE_SUCCESS_SYMBOL = "";

    public static final int SSH_RESPONSE_TIMEOUT = 15;
    /**
     * How often SshClient send keep alive packet
     */
    public static final int SSH_CLIENT_KEEP_ALIVE_INTERVAL = 15;
}
