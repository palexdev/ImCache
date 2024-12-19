package io.github.palexdev.imcache.exceptions;

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
