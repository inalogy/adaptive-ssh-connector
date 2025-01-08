package com.inalogy.midpoint.connector.ssh.exceptions;


public class NoCreateScriptResponseException extends RuntimeException{
    public NoCreateScriptResponseException(){
        super("Received no response from CreateScript.");
    }
}
