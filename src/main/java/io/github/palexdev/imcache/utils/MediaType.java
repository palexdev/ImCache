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

import java.net.URL;
import java.util.*;

/**
 * An enumeration representing various media types (MIME types) for images and videos.
 */
public enum MediaType {
    // Image MIME types
    JPEG("image/jpeg"),
    PNG("image/png"),
    GIF("image/gif"),
    BMP("image/bmp"),
    WEBP("image/webp"),
    SVG("image/svg+xml"),
    TIFF("image/tiff"),
    ICON("image/x-icon"),
    HEIC("image/heic"),
    HEIF("image/heif"),
    JXR("image/jxr"),
    AVIF("image/avif"),

    // Video MIME types
    MP4("video/mp4"),
    WEBM("video/webm"),
    AVI("video/x-msvideo"),
    FLV("video/x-flv"),
    MKV("video/x-matroska"),
    MPEG("video/mpeg"),
    ;

    //================================================================================
    // Static Properties
    //================================================================================
    // For quick lookup
    private static final Map<String, MediaType> MIME_TYPE_MAP = new HashMap<>();
    private static final Set<String> EXTENSIONS_SET = new HashSet<>();

    static {
        for (MediaType type : MediaType.values()) {
            MIME_TYPE_MAP.put(type.getMimeType().toLowerCase(), type);
        }

        Collections.addAll(EXTENSIONS_SET,
            // Image extensions
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "tiff", "tif",
            "ico", "cur", "heic", "heif", "jxr", "wdp", "hdp", "avif",

            // Video extensions
            "mp4", "m4v", "webm", "avi", "flv", "mkv", "mpeg", "mpg"
        );
    }

    //================================================================================
    // Properties
    //================================================================================
    private final String mimeType;


    //================================================================================
    // Constructors
    //================================================================================
    MediaType(String mimeType) {
        this.mimeType = mimeType;
    }

    //================================================================================
    // Static Methods
    //================================================================================

    /// @return whether the given MIME type is supported.
    public static boolean isSupportedMimeType(String mimeType) {
        if (mimeType == null) return false;
        return MIME_TYPE_MAP.containsKey(mimeType.toLowerCase());
    }

    /// @return whether the given URL has a supported extension.
    public static boolean isSupportedExtension(URL url) {
        if (url == null) return false;
        String path = url.getPath();
        int dot = path.lastIndexOf('.');
        if (dot < 0 || dot == path.length() - 1) return false;
        String ext = path.substring(dot + 1).toLowerCase();
        return EXTENSIONS_SET.contains(ext);
    }

    //================================================================================
    // Getters
    //================================================================================
    public String getMimeType() {
        return mimeType;
    }
}
