package com.packx.backend.exception;

/** Thrown when a filename resolves outside of its intended storage directory. */
public class PathTraversalException extends RuntimeException {
    public PathTraversalException(String message) {
        super(message);
    }
}
