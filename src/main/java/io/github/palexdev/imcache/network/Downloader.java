package io.github.palexdev.imcache.network;

import io.github.palexdev.imcache.core.Request;
import io.github.palexdev.imcache.exceptions.ImCacheException;
import io.github.palexdev.imcache.utils.ImageUtils;

import java.io.InputStream;
import java.net.URLConnection;

public class Downloader {

    //================================================================================
    // Constructors
    //================================================================================
    private Downloader() {}

    //================================================================================
    // Static Methods
    //================================================================================
    public static byte[] download(Request request) {
        try {
            // Open connection
            URLConnection connection = request.url().openConnection();
            request.getNetConfig().accept(connection);

            // Transfer to memory
            try (InputStream is = connection.getInputStream()) {
                return ImageUtils.toBytes(null, is);
            }
        } catch (Exception ex) {
            throw new ImCacheException(
                "Failed to download image for request %s"
                    .formatted(request),
                ex
            );
        }
    }
}
