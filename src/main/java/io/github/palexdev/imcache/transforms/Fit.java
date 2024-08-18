package io.github.palexdev.imcache.transforms;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Fit implements Transform {
    //================================================================================
    // Properties
    //================================================================================
    private final double maxWidth;
    private final double maxHeight;

    //================================================================================
    // Constructors
    //================================================================================
    public Fit(double maxWidth, double maxHeight) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public BufferedImage transform(BufferedImage src) {
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();

        double wRatio = maxWidth / srcWidth;
        double hRatio = maxHeight / srcHeight;
        double scale = Math.min(wRatio, hRatio);

        int newWidth = (int) (srcWidth * scale);
        int newHeight = (int) (srcHeight * scale);

        BufferedImage scaled = new BufferedImage(newWidth, newHeight, src.getType());
        Graphics2D g = scaled.createGraphics();
        g.drawImage(src, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return scaled;
    }
}
