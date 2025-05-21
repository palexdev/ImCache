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
