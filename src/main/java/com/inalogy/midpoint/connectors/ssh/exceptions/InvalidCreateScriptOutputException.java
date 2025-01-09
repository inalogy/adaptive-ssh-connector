package com.inalogy.midpoint.connectors.ssh.exceptions;

public class InvalidCreateScriptOutputException extends RuntimeException {
    public InvalidCreateScriptOutputException(String stackTrace){
        super(stackTrace);
    }
}
