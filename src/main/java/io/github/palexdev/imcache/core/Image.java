package io.github.palexdev.imcache.core;

import io.github.palexdev.imcache.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

public class Image {
    //================================================================================
    // Properties
    //================================================================================
    private final URL url;
    private final byte[] data;

    //================================================================================
    // Constructors
    //================================================================================
    public Image(URL url, byte[] data) {
        this.url = url;
        this.data = data;
    }

    public static Image wrap(URL url, byte[] data) {
        return new Image(url, data);
    }

    //================================================================================
    // Getters
    //================================================================================
    public URL url() {return url;}

    public byte[] rawData() {
        return data;
    }

    public BufferedImage asImage() {
        return ImageUtils.toImage(data);
    }

    public InputStream asStream() {
        return ImageUtils.toStream(null, data);
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Image image = (Image) o;
        return Objects.equals(url, image.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public String toString() {
        return "Image{" +
            "url='" + url + '\'' +
            '}';
    }
}
