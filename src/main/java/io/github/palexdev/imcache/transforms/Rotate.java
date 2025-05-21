package io.github.palexdev.imcache.transforms;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/// The [Rotate] class applies a rotation transformation to a given [BufferedImage].
///
/// This transformation rotates the input image by a specified angle (in degrees) around its center.
///
/// Positive values for the rotation angle represent clockwise rotation, while negative values represent counterclockwise rotation.
public class Rotate implements Transform {
    //================================================================================
    // Properties
    //================================================================================
    private final double rotation;

    //================================================================================
    // Constructors
    //================================================================================
    public Rotate(double rotation) {
        this.rotation = rotation;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public BufferedImage transform(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage rotated = new BufferedImage(w, h, src.getType());
        Graphics2D g = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.rotate(Math.toRadians(rotation), w / 2.0, h / 2.0);
        g.setTransform(at);
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return rotated;
    }
}
