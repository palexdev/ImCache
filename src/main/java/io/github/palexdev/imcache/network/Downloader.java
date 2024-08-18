package io.github.palexdev.imcache.network;

import io.github.palexdev.imcache.core.ImRequest;
import io.github.palexdev.imcache.exceptions.ImCacheException;
import io.github.palexdev.imcache.utils.ImageUtils;
import io.github.palexdev.imcache.utils.MediaType;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

public class Downloader {

    //================================================================================
    // Constructors
    //================================================================================
    private Downloader() {}

    //================================================================================
    // Static Methods
    //================================================================================
    public static byte[] download(ImRequest request) {
        try {
            // Open connection
            URL url = new URI(request.url()).toURL();
            URLConnection connection = url.openConnection();
            request.getNetConfig().accept(connection);
            verify(connection);

            // Transfer to memory
            try (InputStream is = connection.getInputStream()) {
                return ImageUtils.toBytes(null, is);
            }
        } catch (Exception ex) {
            throw new ImCacheException(
                "Failed to download image for request %s because: %s"
                    .formatted(request, ex.getMessage()),
                ex
            );
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
