package io.github.palexdev.imcache.transforms;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Flip implements Transform {
    //================================================================================
    // Properties
    //================================================================================
    private final FlipOrientation orientation;

    //================================================================================
    // Constructors
    //================================================================================
    public Flip(FlipOrientation orientation) {
        this.orientation = orientation;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public BufferedImage transform(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage flipped = new BufferedImage(w, h, src.getType());
        Graphics2D g = flipped.createGraphics();

        double scaleX, scaleY, translateX, translateY;
        switch (orientation) {
            case HORIZONTAL -> {
                scaleX = -1.0;
                scaleY = 1.0;
                translateX = -w;
                translateY = 0.0;
            }
            case VERTICAL -> {
                scaleX = 1.0;
                scaleY = -1.0;
                translateX = 0.0;
                translateY = -h;
            }
            default -> throw new IllegalStateException("Unexpected value: " + orientation);
        }
        AffineTransform at = new AffineTransform();
        at.scale(scaleX, scaleY);
        at.translate(translateX, translateY);
        g.setTransform(at);
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return flipped;
    }

    //================================================================================
    // Internal Classes
    //================================================================================
    public enum FlipOrientation {
        HORIZONTAL,
        VERTICAL
    }
}
