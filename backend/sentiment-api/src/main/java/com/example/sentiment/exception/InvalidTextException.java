package com.example.sentiment.exception;

public class InvalidTextException extends RuntimeException {

    public InvalidTextException(String message) {
        super(message);
    }
}
