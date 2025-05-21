package io.github.palexdev.imcache.cache;

import io.github.palexdev.imcache.exceptions.ImCacheException;
import io.github.palexdev.imcache.utils.URLHandler;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.UUID;

/// The `WithID` interface defines an object that can return a unique identifier.
/// It is commonly used in scenarios where unique identification of an item is required.
///
/// This interface also provides utility methods to generate unique identifiers based on different input types such as
/// [String], [URL], [File], or [Path].
///
/// These identifiers are generated using [UUID] based on the input's byte representation to ensure uniqueness.
public interface WithID {
    String id();

    /// Generates a unique identifier with [UUID#nameUUIDFromBytes(byte\[\])].
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

    /// Delegates to [#generateId(String)] by converting the input with [URL#toString()].
    static String generateId(URL url) {
        return generateId(url.toString());
    }

    /// Delegates to [#generateId(URL)] by converting the input [File] to a [URL] with [URLHandler#toURL(File)].
    ///
    /// @throws ImCacheException if the conversion fails
    static String generateId(File file) {
        return generateId(URLHandler.toURL(file).orElseThrow(() -> new ImCacheException("Failed to convert file to url")));
    }

    /// Delegates to [#generateId(File)] by converting the input [Path] to a [File].
    static String generateId(Path path) {
        return generateId(path.toFile());
    }
}
