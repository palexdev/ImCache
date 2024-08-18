package io.github.palexdev.imcache.network;

import io.github.palexdev.imcache.core.ImRequest;
import io.github.palexdev.imcache.exceptions.ImCacheException;
import io.github.palexdev.imcache.utils.ImageUtils;
import io.github.palexdev.imcache.utils.MediaType;

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
    public static byte[] download(ImRequest request) {
        try {
            // Open connection
            URLConnection connection = request.url().openConnection();
            request.getNetConfig().accept(connection);
            if (!verify(connection)) {
                throw new ImCacheException(
                    "Failed to download resource because of invalid url %s"
                        .formatted(request.url())
                );
            }

            // Transfer to memory
            try (InputStream is = connection.getInputStream()) {
                return ImageUtils.toBytes(null, is);
            }
        } catch (ImCacheException ice) {
            throw ice;
        } catch (Exception ex) {
            throw new ImCacheException(
                "Failed to download image for request %s"
                    .formatted(request),
                ex
            );
        }
    }

    private static boolean verify(URLConnection connection) {
        String type = connection.getContentType();
        return MediaType.isSupportedMimeType(type);
    }
}
