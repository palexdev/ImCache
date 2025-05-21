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

/// The [Contrast] class represents an image transformation that adjusts the contrast of an input [BufferedImage].
///
/// This transform modifies the intensity of colors in an image relative to a specified contrast level.
/// Higher contrast values enhance the intensity differences between colors, while lower values reduce the differences,
/// potentially leading to a flatter image.
///
/// The contrast adjustment is centered around a midpoint value of 128
/// for each color channel (red, green, blue).
public class Contrast implements Transform {
    //================================================================================
    // Properties
    //================================================================================
    private final float contrast;

    //================================================================================
    // Constructors
    //================================================================================
    public Contrast(float contrast) {
        this.contrast = contrast;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public BufferedImage transform(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, src.getType());
        int midpoint = 128;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color color = new Color(src.getRGB(x, y));
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();

                // Apply contrast adjustment
                red = Math.clamp((int) ((red - midpoint) * contrast + midpoint), 0, 255);
                green = Math.clamp((int) ((green - midpoint) * contrast + midpoint), 0, 255);
                blue = Math.clamp((int) ((blue - midpoint) * contrast + midpoint), 0, 255);

                Color newColor = new Color(red, green, blue);
                out.setRGB(x, y, newColor.getRGB());
            }
        }
        return out;
    }
}
