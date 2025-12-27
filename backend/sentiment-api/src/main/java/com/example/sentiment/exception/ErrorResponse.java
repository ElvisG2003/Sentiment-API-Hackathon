package com.example.sentiment.exception;

import java.time.LocalDateTime;

public class ErrorResponse {

    private String error;
    private int status;
    private String timestamp;

    public ErrorResponse(String error, int status) {
        this.error = error;
        this.status = status;
        this.timestamp = LocalDateTime.now().toString();
    }

    public String getError() {
        return error;
    }

    public int getStatus() {
        return status;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
