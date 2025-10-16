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
