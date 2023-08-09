package com.inalogy.midpoint.connectors.utils;

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
    public static final int SSH_RESPONSE_TIMEOUT = 15;
    /**
     * How often SshClient send keep alive packet
     */
    public static final int SSH_CLIENT_KEEP_ALIVE_INTERVAL = 15;
}
