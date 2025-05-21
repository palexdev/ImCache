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

/// A transformation class that converts a source [BufferedImage] into its grayscale equivalent.
/// This transformation applies a weighted sum of the red, green, and blue channels of each pixel,
/// producing a single intensity value for each pixel.
///
/// The weights used for the grayscale conversion are:
/// - Red channel: 0.299
/// - Green channel: 0.587
/// - Blue channel: 0.114
///
/// These weights are based on the luminosity method, which considers the human eye's sensitivity
/// to different wavelengths of light.
public class Grayscale implements Transform {

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public BufferedImage transform(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage grayed = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color color = new Color(src.getRGB(x, y));
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();

                // Calculate the grayscale value using a weighted sum
                int gray = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
                Color grayColor = new Color(gray, gray, gray);

                grayed.setRGB(x, y, grayColor.getRGB());
            }
        }
        return grayed;
    }
}
