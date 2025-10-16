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


import io.github.palexdev.imcache.core.ImRequest;
import io.github.palexdev.imcache.exceptions.ImCacheException;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;

/**
 * Utility class for handling URLs.
 */
public class URLHandler {

    //================================================================================
    // Constructors
    //================================================================================
    private URLHandler() {}

    //================================================================================
    // Static Methods
    //================================================================================

    /// Opens a connection to the given url, verifies that the content type is supported, configures the connection
    /// with the given consumer and finally transfers the resource's content to a byte array using [ImageUtils#toBytes(String, Object)].
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

    /// Delegates to [#resolve(URL, ThrowingConsumer)] by using [ImRequest#url()] and [ImRequest#getUrlConfig()].
    public static byte[] resolve(ImRequest request) {
        return resolve(request.url(), request.getUrlConfig());
    }

    /// Creates a [URL] from the given string using [URI#create(String)] and [URI#toURL()].
    public static Optional<URL> toURL(String url) {
        try {
            return Optional.of(URI.create(url).toURL());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    /// Converts the given [File] to a [URL] using [File#toURI()] and [URI#toURL()].
    public static Optional<URL> toURL(File file) {
        try {
            return Optional.of(file.toURI().toURL());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    /// Retrieves the content type from the given [URLConnection] ([URLConnection#getContentType()]) and then delegates
    /// to [MediaType#isSupportedMimeType(String)] and [MediaType#isSupportedExtension(URL)] to ensure the resource is
    /// valid and supported.
    private static void verify(URLConnection connection) throws ImCacheException {
        String type = connection.getContentType();
        if (!MediaType.isSupportedMimeType(type) && !MediaType.isSupportedExtension(connection.getURL()))
            throw new ImCacheException(
                "Unsupported MIME type %s for url %s"
                    .formatted(type, connection.getURL())
            );
    }
}
