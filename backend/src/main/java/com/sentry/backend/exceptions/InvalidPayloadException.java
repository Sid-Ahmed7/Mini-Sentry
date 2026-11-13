package com.sentry.backend.exceptions;

public class InvalidPayloadException extends RuntimeException{

    public InvalidPayloadException(String message) {
        super(message);
    }
    
}
