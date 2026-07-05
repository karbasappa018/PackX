package com.packx.backend.exception;

/** Thrown when an uploaded "packed" file does not match the PackX format. */
public class InvalidPackedFileException extends RuntimeException {
    public InvalidPackedFileException(String message) {
        super(message);
    }
}
