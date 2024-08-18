package io.github.palexdev.imcache.transforms;

import java.awt.*;
import java.awt.image.BufferedImage;

public class AspectRatioCrop implements Transform {
    //================================================================================
    // Properties
    //================================================================================
    private final int targetWidthRatio;
    private final int targetHeightRatio;

    //================================================================================
    // Constructors
    //================================================================================
    public AspectRatioCrop(int targetWidthRatio, int targetHeightRatio) {
        this.targetWidthRatio = targetWidthRatio;
        this.targetHeightRatio = targetHeightRatio;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public BufferedImage transform(BufferedImage src) {
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        double targetAspectRatio = (double) targetWidthRatio / targetHeightRatio;

        // Crop area
        int cropWidth, cropHeight;
        if ((double) srcWidth / srcHeight > targetAspectRatio) {
            // Image is wider than the target aspect ratio
            cropHeight = srcHeight;
            cropWidth = (int) (srcHeight * targetAspectRatio);
        } else {
            // Image is taller than the target aspect ratio
            cropWidth = srcWidth;
            cropHeight = (int) (srcWidth / targetAspectRatio);
        }
        int x = (srcWidth - cropWidth) / 2;
        int y = (srcHeight - cropHeight) / 2;

        // Perform crop
        BufferedImage cropped = src.getSubimage(x, y, cropWidth, cropHeight);
        BufferedImage out = new BufferedImage(cropped.getWidth(), cropped.getHeight(), src.getType());
        Graphics2D g = out.createGraphics();
        g.drawImage(cropped, 0, 0, null);
        g.dispose();
        return out;
    }
}
