package com.inalogy.midpoint.connectors.utils;

public class Constants {
    public static final String TYPE_POWERSHELL = "powershell";
    public static final String TYPE_SHELL = "shell";
    public static final String CLEAR_COMMAND_UNIX = "clear";
    public static final String CLEAR_COMMAND_WINDOWS = "cls";
    public static final String RESPONSE_SEPARATOR = "|";
    public static final String RESPONSE_EMPTY_ATTRIBUTE_SYMBOL = "null";

    public static final String SPECIAL_CONNID_NAME = "__NAME__";
    public static final String SPECIAL_CONNID_UID = "__UID__";
    public static final String SPECIAL_CONNID_PASSWORD = "__PASSWORD__";
    public static final String MICROSOFT_EXCHANGE_NAME_FLAG = "name";
    public static final String MICROSOFT_EXCHANGE_EMAIL_FLAG = "email";
    public static final String MICROSOFT_EXCHANGE_PASSWORD_FLAG = "password";
    public static final String MICROSOFT_EXCHANGE_MULTIVALUED_SEPARATOR = " "; // maybe not good idea to use space
    public static final String MICROSOFT_EXCHANGE_ADD_UPDATEDELTA = "ADD:";
    public static final String MICROSOFT_EXCHANGE_REMOVE_UPDATEDELTA = "REMOVE:";
    public static final int SSH_RESPONSE_TIMEOUT = 30;
    public static final int SSH_CLIENT_KEEP_ALIVE_TIMEOUT = 15;
}
