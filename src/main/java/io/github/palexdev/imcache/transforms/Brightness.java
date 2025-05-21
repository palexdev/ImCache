package io.github.palexdev.imcache.transforms;

import java.awt.*;
import java.awt.image.BufferedImage;

/// The [Brightness] class implements the [Transform] interface and provides functionality to adjust the brightness level
/// of a given image.
///
/// This transformation involves modifying the RGB values of each pixel by adding a specific brightness value,
/// clamping the result to ensure the values remain within the valid range [0, 255].
///
/// For both negative and positive brightness values:
/// - A positive value increases the brightness of the image.
/// - A negative value darkens the image.
public class Brightness implements Transform {
    //================================================================================
    // Properties
    //================================================================================
    private final int brightness;

    //================================================================================
    // Constructors
    //================================================================================
    public Brightness(int brightness) {
        this.brightness = brightness;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public BufferedImage transform(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, src.getType());
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color color = new Color(src.getRGB(x, y));
                int red = Math.clamp(color.getRed() + brightness, 0, 255);
                int green = Math.clamp(color.getGreen() + brightness, 0, 255);
                int blue = Math.clamp(color.getBlue() + brightness, 0, 255);

                Color newColor = new Color(red, green, blue);
                out.setRGB(x, y, newColor.getRGB());
            }
        }
        return out;
    }
}
