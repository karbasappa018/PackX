package com.packx.backend.exception;

/** Thrown when a requested operation id does not exist. */
public class OperationNotFoundException extends RuntimeException {
    public OperationNotFoundException(String message) {
        super(message);
    }
}
