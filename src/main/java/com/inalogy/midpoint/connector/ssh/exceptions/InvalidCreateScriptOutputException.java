package com.inalogy.midpoint.connector.ssh.exceptions;

public class InvalidCreateScriptOutputException extends RuntimeException {
    public InvalidCreateScriptOutputException(String stackTrace){
        super(stackTrace);
    }
}
