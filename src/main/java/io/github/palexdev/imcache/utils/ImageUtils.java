package io.github.palexdev.imcache.utils;

import io.github.palexdev.imcache.exceptions.ImCacheException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    //================================================================================
    // Constructors
    //================================================================================
    private ImageUtils() {}

    //================================================================================
    // Static Methods
    //================================================================================
    public static byte[] toBytes(String format, Object data) {
        try {
            return switch (data) {
                case byte[] arr -> arr;
                case BufferedImage bi -> {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    ImageIO.write(bi, format, out);
                    yield out.toByteArray();
                }
                case InputStream is -> {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int cnt;
                    while ((cnt = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, cnt);
                    }
                    yield baos.toByteArray();
                }
                case null, default -> new byte[]{};
            };
        } catch (IOException ex) {
            throw new ImCacheException(
                "Failed to convert image from type %s to bytes array"
                    .formatted(data.getClass()),
                ex
            );
        }
    }

    public static BufferedImage toImage(Object data) {
        try {
            return switch (data) {
                case byte[] arr -> ImageIO.read(new ByteArrayInputStream(arr));
                case InputStream is -> ImageIO.read(is);
                case null, default -> null;
            };
        } catch (IOException ex) {
            throw new ImCacheException(
                "Failed to convert image from type %s to BufferedImage"
                    .formatted(data.getClass()),
                ex
            );
        }
    }

    public static InputStream toStream(String format, Object data) {
        try {
            return switch (data) {
                case byte[] arr -> new ByteArrayInputStream(arr);
                case BufferedImage bi -> {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bi, format, baos);
                    yield new ByteArrayInputStream(toBytes(format, data));
                }
                case null, default -> InputStream.nullInputStream();
            };
        } catch (IOException ex) {
            throw new ImCacheException(
                "Failed to convert image from type %s to InputStream"
                    .formatted(data.getClass()),
                ex
            );
        }
    }
}
