package io.github.palexdev.imcache.transforms;

import java.awt.image.BufferedImage;

public class CenterCrop implements Transform {
    //================================================================================
    // Properties
    //================================================================================
    private final double targetWidth;
    private final double targetHeight;

    //================================================================================
    // Constructors
    //================================================================================
    public CenterCrop(double targetWidth, double targetHeight) {
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public BufferedImage transform(BufferedImage src) {
        double sourceWidth = src.getWidth();
        double sourceHeight = src.getHeight();
        if (sourceWidth == targetWidth && sourceHeight == targetHeight)
            return src;

        double xScale = targetWidth / sourceWidth;
        double yScale = targetHeight / sourceHeight;
        double newXScale, newYScale;
        if (xScale < yScale) {
            newXScale = (1.0 / yScale) * xScale;
            newYScale = 1.0;
        } else {
            newXScale = 1.0;
            newYScale = (1.0 / xScale) * yScale;
        }

        double scaledWidth = sourceWidth * newXScale;
        double scaledHeight = sourceHeight * newYScale;
        double left = (sourceWidth - scaledWidth) / 2;
        double top = (sourceHeight - scaledHeight) / 2;
        BufferedImage centered = src.getSubimage(
            (int) left,
            (int) top,
            (int) scaledWidth,
            (int) scaledHeight
        );
        return new Resize(targetWidth, targetHeight).transform(centered);
    }
}
