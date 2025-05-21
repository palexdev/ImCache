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

/// The [Brightness] class implements the [Transform] interface and provides functionality to adjust the brightness level
/// of a given image.
///
/// This transformation involves modifying the RGB values of each pixel by adding a specific brightness value,
/// clamping the result to ensure the values remain within the valid range [0, 255].
///
/// For both negative and positive brightness values:
/// - A positive value increases the brightness of the image.
/// - A negative value darkens the image.
public class Brightness implements Transform {
    //================================================================================
    // Properties
    //================================================================================
    private final int brightness;

    //================================================================================
    // Constructors
    //================================================================================
    public Brightness(int brightness) {
        this.brightness = brightness;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public BufferedImage transform(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, src.getType());
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color color = new Color(src.getRGB(x, y));
                int red = Math.clamp(color.getRed() + brightness, 0, 255);
                int green = Math.clamp(color.getGreen() + brightness, 0, 255);
                int blue = Math.clamp(color.getBlue() + brightness, 0, 255);

                Color newColor = new Color(red, green, blue);
                out.setRGB(x, y, newColor.getRGB());
            }
        }
        return out;
    }
}
