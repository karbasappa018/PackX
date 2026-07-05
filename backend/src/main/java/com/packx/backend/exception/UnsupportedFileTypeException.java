package com.packx.backend.exception;

/** Thrown when a file extension is not in the allow-list. */
public class UnsupportedFileTypeException extends RuntimeException {
    public UnsupportedFileTypeException(String message) {
        super(message);
    }
}
