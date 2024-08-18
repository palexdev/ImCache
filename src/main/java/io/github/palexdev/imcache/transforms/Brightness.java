package io.github.palexdev.imcache.transforms;

import java.awt.*;
import java.awt.image.BufferedImage;

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
