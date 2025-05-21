/*
 * Copyright (C) 2025 Parisi Alessandro - alessandro.parisi406@gmail.com
 * This file is part of ImCache (https://github.com/palexdev/imcache)
 *
 * ImCache is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * ImCache is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ImCache. If not, see <http://www.gnu.org/licenses/>.
 */

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
