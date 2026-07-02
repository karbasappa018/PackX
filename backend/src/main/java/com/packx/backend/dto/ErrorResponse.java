package com.packx.backend.dto;

import java.time.Instant;

public class ErrorResponse {
    private boolean success = false;
    private String message;
    private Instant timestamp = Instant.now();

    public ErrorResponse() {
    }

    public ErrorResponse(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
