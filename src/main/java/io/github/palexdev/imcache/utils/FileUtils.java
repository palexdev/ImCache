package io.github.palexdev.imcache.utils;

import io.github.palexdev.imcache.exceptions.ImCacheException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/// Utility class for file operations.
public class FileUtils {

    //================================================================================
    // Constructors
    //================================================================================
    private FileUtils() {}

    //================================================================================
    // Static Methods
    //================================================================================

    /// Reads the file's content into a byte array using [Files#readAllBytes(Path)].
    ///
    /// @throws ImCacheException if an error occurs while reading the file
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

    /// Delegates to [#read(Path)] by converting the input with [File#toPath()].
    public static byte[] read(File file) {
        return read(file.toPath());
    }
}
