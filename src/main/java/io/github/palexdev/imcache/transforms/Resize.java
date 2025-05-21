package io.github.palexdev.imcache.transforms;

import java.awt.*;
import java.awt.image.BufferedImage;

/// The [Resize] class scales a given [BufferedImage] to the specified target width and height, potentially altering
/// the aspect ratio if the width and height ratios do not match.
///
/// The resizing process uses smooth scaling to improve the visual quality of the output image.
public class Resize implements Transform {
    //================================================================================
    // Properties
    //================================================================================
    private final double targetWidth;
    private final double targetHeight;

    //================================================================================
    // Constructors
    //================================================================================
    public Resize(double targetWidth, double targetHeight) {
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================

    @Override
    public BufferedImage transform(BufferedImage src) {
        int width = (int) targetWidth;
        int height = (int) targetHeight;
        if (src.getWidth() == width && src.getHeight() == height)
            return src;

        Image tmp = src.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.drawImage(tmp, 0, 0, null);
        g.dispose();
        return resized;
    }
}
