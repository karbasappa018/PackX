package com.packx.backend.exception;

/** Thrown for request-level validation failures (empty upload, duplicate filename, etc). */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
