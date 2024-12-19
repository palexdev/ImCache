package io.github.palexdev.imcache.utils;


import java.io.InputStream;
import java.net.*;
import java.util.Optional;

import io.github.palexdev.imcache.core.ImRequest;
import io.github.palexdev.imcache.exceptions.ImCacheException;

public class URLHandler {

    //================================================================================
    // Constructors
    //================================================================================
    private URLHandler() {}

    //================================================================================
    // Static Methods
    //================================================================================
    public static byte[] resolve(URL url, ThrowingConsumer<URLConnection> urlConfig) {
        try {
            // Open connection
            URLConnection connection = url.openConnection();
            verify(connection);
            if (urlConfig != null) urlConfig.accept(connection);

            // Transfer to memory
            try (InputStream is = connection.getInputStream()) {
                return ImageUtils.toBytes(null, is);
            }
        } catch (Exception ex) {
            throw new ImCacheException(
                "Failed to resolve url %s because: %s"
                    .formatted(url, ex.getMessage()),
                ex
            );
        }
    }

    public static byte[] resolve(ImRequest request) {
        return resolve(request.url(), request.getUrlConfig());
    }

    public static Optional<URL> toURL(String url) {
        try {
            return Optional.of(URI.create(url).toURL());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private static void verify(URLConnection connection) throws ImCacheException {
        String type = connection.getContentType();
        if (!MediaType.isSupportedMimeType(type))
            throw new ImCacheException(
                "Unsupported MIME type %s for url %s"
                    .formatted(type, connection.getURL())
            );
    }
}
