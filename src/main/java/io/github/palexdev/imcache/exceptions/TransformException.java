package io.github.palexdev.imcache.exceptions;

/// Custom exception thrown to indicate that an error occurred during a transform operation.
public class TransformException extends RuntimeException {

    //================================================================================
    // Constructors
    //================================================================================
    public TransformException(String message) {
        super(message);
    }
}
