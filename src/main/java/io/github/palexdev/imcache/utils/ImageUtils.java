package io.github.palexdev.imcache.utils;

import io.github.palexdev.imcache.core.ImImage;
import io.github.palexdev.imcache.exceptions.ImCacheException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageUtils {
    //================================================================================
    // Static Properties
    //================================================================================
    public static final byte FILE_FORMAT_VERSION = 1;

    //================================================================================
    // Constructors
    //================================================================================
    private ImageUtils() {}

    //================================================================================
    // Static Methods
    //================================================================================
    public static void serialize(ImImage img, File file) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
            // Write header
            dos.writeByte(FILE_FORMAT_VERSION);  // Format version
            dos.writeInt(img.url().length());    // URL length
            dos.writeInt(img.rawData().length);  // Data length

            // Write data
            dos.writeBytes(img.url());           // Request URL
            dos.write(img.rawData());            // Image data
        }
    }

    public static ImImage deserialize(File file) throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            // Read the header
            byte version = dis.readByte();       // Check compatible version
            if (version != FILE_FORMAT_VERSION)
                throw new IOException("Invalid format version %s".formatted(version));

            int urlLength = dis.readInt();       // Read URL length
            int dataLength = dis.readInt();      // Read data length

            // Read data and build ImImage object
            byte[] urlBytes = new byte[urlLength];
            dis.readFully(urlBytes);
            String url = new String(urlBytes);   // Convert URL bytes to string

            byte[] rawData = new byte[dataLength];
            dis.readFully(rawData);

            return ImImage.wrap(url, rawData);
        }
    }

    public static byte[] toBytes(String format, Object data) {
        try {
            return switch (data) {
                case byte[] arr -> arr;
                case BufferedImage bi -> {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    ImageIO.write(bi, format, out);
                    if (out.size() == 0) throw new ImCacheException("Conversion from BufferedImage to byte[] failed");
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
                    if (baos.size() == 0) throw new ImCacheException("Conversion from BufferedImage to InputStream failed");
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
