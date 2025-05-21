package io.github.palexdev.imcache.transforms;

import java.awt.*;
import java.awt.image.BufferedImage;

/// A transformation class that converts a source [BufferedImage] into its grayscale equivalent.
/// This transformation applies a weighted sum of the red, green, and blue channels of each pixel,
/// producing a single intensity value for each pixel.
///
/// The weights used for the grayscale conversion are:
/// - Red channel: 0.299
/// - Green channel: 0.587
/// - Blue channel: 0.114
///
/// These weights are based on the luminosity method, which considers the human eye's sensitivity
/// to different wavelengths of light.
public class Grayscale implements Transform {

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public BufferedImage transform(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage grayed = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color color = new Color(src.getRGB(x, y));
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();

                // Calculate the grayscale value using a weighted sum
                int gray = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
                Color grayColor = new Color(gray, gray, gray);

                grayed.setRGB(x, y, grayColor.getRGB());
            }
        }
        return grayed;
    }
}
