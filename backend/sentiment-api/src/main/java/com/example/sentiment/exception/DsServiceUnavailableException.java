package com.example.sentiment.exception;

public class DsServiceUnavailableException extends RuntimeException {

    public DsServiceUnavailableException(String message) {
        super(message);
    }

    public DsServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }


}
