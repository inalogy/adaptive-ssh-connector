package com.inalogy.midpoint.connectors.ssh.exceptions;

public class InvalidSearchScriptOutputException extends RuntimeException {
    public InvalidSearchScriptOutputException(String message) {
        super(message);
    }

    public InvalidSearchScriptOutputException(String message, Throwable cause) {
        super(message, cause);
    }
}