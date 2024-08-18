package io.github.palexdev.imcache.transforms;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Pad implements Transform {
    //================================================================================
    // Properties
    //================================================================================
    private final double targetWidth;
    private final double targetHeight;
    private final Color paddingColor;

    //================================================================================
    // Constructors
    //================================================================================
    public Pad(double targetWidth, double targetHeight) {
        this(targetWidth, targetHeight, Color.WHITE);
    }

    public Pad(double targetWidth, double targetHeight, Color paddingColor) {
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        this.paddingColor = paddingColor;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public BufferedImage transform(BufferedImage src) {
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int targetWidth = (int) this.targetWidth;
        int targetHeight = (int) this.targetHeight;

        BufferedImage padded = new BufferedImage(targetWidth, targetHeight, src.getType());
        Graphics2D g = padded.createGraphics();
        g.setColor(paddingColor);
        g.fillRect(0, 0, targetWidth, targetHeight);

        double wRatio = (double) targetWidth / srcWidth;
        double hRatio = (double) targetHeight / srcHeight;
        double scale = Math.min(wRatio, hRatio);

        int newWidth = (int) (srcWidth * scale);
        int newHeight = (int) (srcHeight * scale);

        int x = (targetWidth - newWidth) / 2;
        int y = (targetHeight - newHeight) / 2;

        g.drawImage(src, x, y, newWidth, newHeight, null);
        g.dispose();
        return padded;
    }
}
