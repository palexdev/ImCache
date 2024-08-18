package io.github.palexdev.imcache.utils;

import java.util.HashMap;
import java.util.Map;

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

    static {
        for (MediaType type : MediaType.values()) {
            MIME_TYPE_MAP.put(type.getMimeType().toLowerCase(), type);
        }
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

    public static boolean isSupportedMimeType(String mimeType) {
        return MIME_TYPE_MAP.containsKey(mimeType.toLowerCase());
    }

    //================================================================================
    // Getters
    //================================================================================
    public String getMimeType() {
        return mimeType;
    }
}
