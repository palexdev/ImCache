package io.github.palexdev.imcache.transforms;

import java.awt.*;
import java.awt.image.BufferedImage;

/// The [Contrast] class represents an image transformation that adjusts the contrast of an input [BufferedImage].
///
/// This transform modifies the intensity of colors in an image relative to a specified contrast level.
/// Higher contrast values enhance the intensity differences between colors, while lower values reduce the differences,
/// potentially leading to a flatter image.
///
/// The contrast adjustment is centered around a midpoint value of 128
/// for each color channel (red, green, blue).
public class Contrast implements Transform {
    //================================================================================
    // Properties
    //================================================================================
    private final float contrast;

    //================================================================================
    // Constructors
    //================================================================================
    public Contrast(float contrast) {
        this.contrast = contrast;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public BufferedImage transform(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, src.getType());
        int midpoint = 128;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color color = new Color(src.getRGB(x, y));
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();

                // Apply contrast adjustment
                red = Math.clamp((int) ((red - midpoint) * contrast + midpoint), 0, 255);
                green = Math.clamp((int) ((green - midpoint) * contrast + midpoint), 0, 255);
                blue = Math.clamp((int) ((blue - midpoint) * contrast + midpoint), 0, 255);

                Color newColor = new Color(red, green, blue);
                out.setRGB(x, y, newColor.getRGB());
            }
        }
        return out;
    }
}
