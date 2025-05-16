package com.inalogy.midpoint.connectors.ssh.utils;

/**
 * A class that defines constant values used throughout the project.
 * @author P-Rovnak
 * @since 1.0
 */
public class Constants {
    public static final String TYPE_POWERSHELL = "powershell";
    public static final String TYPE_SHELL = "shell";
    public static final String TYPE_CSH_SHELL = "cshell";
    public static final String TYPE_KORN_SHELL = "kornshell";
    public static final String TYPE_BASH_SHELL = "bashshell";
    public static final String SEARCH_OPERATION = "searchOperation";
    public static final String UPDATE_OPERATION = "updateOperation";
    public static final String CREATE_OPERATION = "createOperation";
    public static final String DELETE_OPERATION = "deleteOperation";

    public static final String SPECIAL_CONNID_NAME = "__NAME__";
    public static final String SPECIAL_CONNID_UID = "__UID__";
    public static final String SPECIAL_CONNID_PASSWORD = "__PASSWORD__";

    /**
     * How often SshClient send keep alive packet
     */
    public static final int SSH_CLIENT_KEEP_ALIVE_INTERVAL = 15;

    public static final String ATTR_DETAILS_CREATABLE = "creatable";
    public static final String ATTR_DETAILS_UPDATEABLE = "updateable";
    public static final String ATTR_DETAILS_READABLE = "readable";
    public static final String ATTR_DETAILS_MULTIVALUED = "multivalued";
    public static final String ATTR_DETAILS_DATA_TYPE = "dataType";
    public static final String ATTR_DETAILS_REQUIRED = "required";
    public static final String ATTR_DETAILS_RETURNED_BY_DEFAULT = "returnedByDefault";

}
