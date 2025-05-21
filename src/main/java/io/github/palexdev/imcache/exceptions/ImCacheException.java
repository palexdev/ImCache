package io.github.palexdev.imcache.exceptions;

/// Custom exception for handling errors that occur within the `ImCache` library.
public class ImCacheException extends RuntimeException {

    //================================================================================
    // Constructors
    //================================================================================
    public ImCacheException(Throwable cause) {
        super(cause);
    }

    public ImCacheException(String message) {
        super(message);
    }

    public ImCacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
