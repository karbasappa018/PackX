package com.packx.backend.exception;

/** Generic wrapper for I/O or processing failures during pack/unpack. */
public class FileProcessingException extends RuntimeException {
    public FileProcessingException(String message) {
        super(message);
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
