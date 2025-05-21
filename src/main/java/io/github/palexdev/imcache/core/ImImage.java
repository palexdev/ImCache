package io.github.palexdev.imcache.core;

import io.github.palexdev.imcache.utils.ImageUtils;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.function.Function;

/// Wrapper type to represent a loaded image by its source [URL] and its raw byte data.
///
/// A series of convenience methods allow to convert the data to more useful formats.
public class ImImage {
    //================================================================================
    // Properties
    //================================================================================
    private final URL url;
    private final byte[] data;

    //================================================================================
    // Constructors
    //================================================================================
    public ImImage(URL url, byte[] data) {
        this.url = url;
        this.data = data;
    }

    public static ImImage wrap(URL url, byte[] data) {
        return new ImImage(url, data);
    }

    //================================================================================
    // Methods
    //================================================================================

    /// Converts the raw image data in this wrapper to a [BufferedImage] using [ImageUtils#toImage(Object)].
    public BufferedImage asImage() {
        return ImageUtils.toImage(data);
    }

    /// Converts the raw image data in this wrapper to a [InputStream] using [ImageUtils#toStream(String, Object)].
    public InputStream asStream() {
        return ImageUtils.toStream(null, data);
    }

    /// Converts the raw image data in this wrapper to the desired `T` type using the given function.
    public <T> T as(Function<byte[], T> converter) {
        return converter.apply(data);
    }

    //================================================================================
    // Getters
    //================================================================================

    /// @return the image's source [URL]
    public URL url() {
        return url;
    }

    /// @return the loaded image's raw data
    public byte[] rawData() {
        return data;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImImage image = (ImImage) o;
        return Objects.equals(url, image.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public String toString() {
        return "ImImage{" +
               "uri='" + url + '\'' +
               '}';
    }
}
