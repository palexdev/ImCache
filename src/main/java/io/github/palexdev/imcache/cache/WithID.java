package io.github.palexdev.imcache.cache;

import io.github.palexdev.imcache.exceptions.ImCacheException;
import io.github.palexdev.imcache.utils.URLHandler;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.UUID;

public interface WithID {
    String id();

    static String generateId(String s) {
        try {
            return UUID.nameUUIDFromBytes(s.getBytes()).toString();
        } catch (Exception ex) {
            throw new ImCacheException(
                "Failed to generate id from string %s"
                    .formatted(s),
                ex
            );
        }
    }

    static String generateId(URL url) {
        return generateId(url.toString());
    }

    static String generateId(File file) {
        return generateId(URLHandler.toURL(file).orElseThrow());
    }

    static String generateId(Path path) {
        return generateId(path.toFile());
    }
}
