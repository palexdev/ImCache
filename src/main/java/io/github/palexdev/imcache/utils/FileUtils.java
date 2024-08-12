package io.github.palexdev.imcache.utils;

import io.github.palexdev.imcache.exceptions.ImCacheException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {

    //================================================================================
    // Constructors
    //================================================================================
    private FileUtils() {}

    //================================================================================
    // Static Methods
    //================================================================================
    public static byte[] read(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException ex) {
            throw new ImCacheException(
                "Failed to read file: %s"
                    .formatted(path),
                ex
            );
        }
    }

    public static byte[] read(File file) {
        return read(file.toPath());
    }
}
