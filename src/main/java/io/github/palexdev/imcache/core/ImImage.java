package io.github.palexdev.imcache.core;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.function.Function;

import io.github.palexdev.imcache.utils.ImageUtils;

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

    public BufferedImage asImage() {
        return ImageUtils.toImage(data);
    }

    public InputStream asStream() {
        return ImageUtils.toStream(null, data);
    }

    public <T> T as(Function<byte[], T> converter) {
        return converter.apply(data);
    }

    //================================================================================
    // Getters
    //================================================================================

    public URL url() {return url;}

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
