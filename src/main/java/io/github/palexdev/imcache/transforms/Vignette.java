/*
 * Copyright (C) 2025 Parisi Alessandro - alessandro.parisi406@gmail.com
 * This file is part of ImCache (https://github.com/palexdev/imcache)
 *
 * ImCache is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * ImCache is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ImCache. If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.palexdev.imcache.transforms;

import java.awt.*;
import java.awt.image.BufferedImage;

/// The [Vignette] class provides a transformation that applies a vignette effect to the given [BufferedImage].
/// The vignette effect darkens the edges of the image, simulating a gradual fade-out toward the corners, while maintaining
/// the center's original brightness.
public class Vignette implements Transform {

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public BufferedImage transform(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();

        BufferedImage out = new BufferedImage(w, h, src.getType());
        Graphics2D g2 = out.createGraphics();
        g2.drawImage(src, 0, 0, null);

        // Find image center
        Point center = new Point(w / 2, h / 2);
        float maxDistance = (float) center.distance(0, 0);

        // Create the vignette gradient
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float distance = (float) center.distance(x, y);
                float ratio = distance / maxDistance;
                ratio = Math.min(ratio, 1.0f);

                Color originalColor = new Color(src.getRGB(x, y), true);
                int alpha = (int) (255 * (1 - ratio));
                int r = (originalColor.getRed() * alpha) / 255;
                int g = (originalColor.getGreen() * alpha) / 255;
                int b = (originalColor.getBlue() * alpha) / 255;
                int a = originalColor.getAlpha();

                Color newColor = new Color(r, g, b, a);
                out.setRGB(x, y, newColor.getRGB());
            }
        }
        g2.dispose();
        return out;
    }
}
