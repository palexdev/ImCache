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

/// The [Fit] class scales an input [BufferedImage] while maintaining its aspect ratio, ensuring that the resulting image
/// dimensions remain within specified maximum width and height bounds.
/// It is designed to proportionally resize the image such that neither the width nor the height exceeds the specified limits,
/// without cropping or distortion.
///
/// The scaling logic computes the aspect ratio for both width and height with respect to the maximum limits,
/// and the smaller ratio is used as the scaling factor to preserve the original aspect ratio of the image.
public class Fit implements Transform {
    //================================================================================
    // Properties
    //================================================================================
    private final double maxWidth;
    private final double maxHeight;

    //================================================================================
    // Constructors
    //================================================================================
    public Fit(double maxWidth, double maxHeight) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public BufferedImage transform(BufferedImage src) {
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();

        double wRatio = maxWidth / srcWidth;
        double hRatio = maxHeight / srcHeight;
        double scale = Math.min(wRatio, hRatio);

        int newWidth = (int) (srcWidth * scale);
        int newHeight = (int) (srcHeight * scale);

        BufferedImage scaled = new BufferedImage(newWidth, newHeight, src.getType());
        Graphics2D g = scaled.createGraphics();
        g.drawImage(src, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return scaled;
    }
}
