package io.github.palexdev.imcache.transforms;

import java.awt.*;
import java.awt.image.BufferedImage;

/// The [Sketch] class implements a stylized edge-detection transformation that simulates a sketch or pencil-drawing
/// effect on an image.
///
/// It works by computing color differences between diagonally adjacent pixels to highlight edges.
/// The result is a grayscale image with enhanced edges, resembling a hand-drawn sketch.
public class Sketch implements Transform {

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public BufferedImage transform(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, src.getType());
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                Color c1 = new Color(src.getRGB(x - 1, y - 1));
                Color c2 = new Color(src.getRGB(x + 1, y + 1));

                int r = Math.abs(c1.getRed() - c2.getRed());
                int g = Math.abs(c1.getGreen() - c2.getGreen());
                int b = Math.abs(c1.getBlue() - c2.getBlue());

                int edgeColor = (r + g + b) / 3;
                edgeColor = Math.min(255, edgeColor * 3); // Enhance the edge

                Color newColor = new Color(edgeColor, edgeColor, edgeColor);
                out.setRGB(x, y, newColor.getRGB());
            }
        }
        return out;
    }
}
