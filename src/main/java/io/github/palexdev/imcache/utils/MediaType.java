package io.github.palexdev.imcache.utils;

import java.net.URL;
import java.util.*;

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
    public static boolean isSupportedMimeType(String mimeType) {
        if (mimeType == null) return false;
        return MIME_TYPE_MAP.containsKey(mimeType.toLowerCase());
    }

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
