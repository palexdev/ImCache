package io.github.palexdev.imcache.utils;

import javax.imageio.ImageIO;

import io.github.palexdev.imcache.core.ImImage;
import io.github.palexdev.imcache.exceptions.ImCacheException;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;

/// Utility class for handling various image-related operations.
///
/// **Note:** `ImCache` saves images on the disk with a custom format which contains the necessary data to reload them back with
/// the same id as when it was requested.
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

    /// Serializes the given `ImImage` object to the given file.
    public static void serialize(ImImage img, File file) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
            String url = img.url().toString();
            // Write header
            dos.writeByte(FILE_FORMAT_VERSION);  // Format version
            dos.writeInt(url.length());          // URL length
            dos.writeInt(img.rawData().length);  // Data length

            // Write data
            dos.writeBytes(url);                 // Request URL
            dos.write(img.rawData());            // Image data
        }
    }

    /// Deserializes the given file to an `ImImage` object only if the format version matches [#FILE_FORMAT_VERSION].
    public static ImImage deserialize(File file) throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            // Read the header
            byte version = dis.readByte();       // Check compatible version
            if (version != FILE_FORMAT_VERSION)
                throw new IOException("Unsupported format version %s. Expected %s".formatted(version, FILE_FORMAT_VERSION));

            int urlLength = dis.readInt();       // Read URL length
            int dataLength = dis.readInt();      // Read data length

            // Read data and build ImImage object
            byte[] urlBytes = new byte[urlLength];
            dis.readFully(urlBytes);
            String url = new String(urlBytes);   // Convert URL bytes to string

            byte[] rawData = new byte[dataLength];
            dis.readFully(rawData);

            return ImImage.wrap(URLHandler.toURL(url).orElse(null), rawData);
        }
    }

    /// Given an image as a generic type, converts it to a byte array.
    ///
    /// Three types are currently supported:
    /// 1) A byte array returns data as is
    /// 2) A [BufferedImage] object is converted using [ImageIO] and the given format,
    /// see [ImageIO#write(RenderedImage, String, OutputStream)].
    /// 3) An [InputStream] object is converted to a byte array using [InputStream#readAllBytes].
    ///
    /// For other types or a `null` object, an empty array is returned.
    public static byte[] toBytes(String format, Object data) {
        try {
            return switch (data) {
                case byte[] arr -> arr;
                case BufferedImage bi -> {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    ImageIO.write(bi, format, out);
                    if (out.size() == 0) throw new IOException("Conversion from BufferedImage to byte[] failed");
                    yield out.toByteArray();
                }
                case InputStream is -> is.readAllBytes();
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

    /// Given an image as a generic type, converts it to a [BufferedImage] object.
    ///
    /// Three types are currently supported:
    /// 1) A [BufferedImage] object returns data as is.
    /// 2) A byte array is converted using [ImageIO#read(InputStream)].
    /// 3) An [InputStream] object is converted to a [BufferedImage] object using [ImageIO#read(InputStream)].
    ///
    /// For other types or a `null` object, `null` is returned.
    public static BufferedImage toImage(Object data) {
        try {
            return switch (data) {
                case BufferedImage bi -> bi;
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

    /// Given an image as a generic type, converts it to an [InputStream] object.
    ///
    /// Three types are currently supported:
    /// 1) An [InputStream] object returns data as is.
    /// 2) A byte array is converted to an [InputStream] object using [ByteArrayInputStream].
    /// 3) A [BufferedImage] object is converted to an [InputStream] object using [ByteArrayInputStream] and
    ///  [ImageIO#write(RenderedImage, String, OutputStream)].
    ///
    /// For other types or a `null` object, an empty [InputStream] is returned.
    public static InputStream toStream(String format, Object data) {
        try {
            return switch (data) {
                case InputStream is -> is;
                case byte[] arr -> new ByteArrayInputStream(arr);
                case BufferedImage bi -> {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bi, format, baos);
                    if (baos.size() == 0)
                        throw new ImCacheException("Conversion from BufferedImage to InputStream failed");
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
