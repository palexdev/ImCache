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

/// The [Resize] class scales a given [BufferedImage] to the specified target width and height, potentially altering
/// the aspect ratio if the width and height ratios do not match.
///
/// The resizing process uses smooth scaling to improve the visual quality of the output image.
public class Resize implements Transform {
    //================================================================================
    // Properties
    //================================================================================
    private final double targetWidth;
    private final double targetHeight;

    //================================================================================
    // Constructors
    //================================================================================
    public Resize(double targetWidth, double targetHeight) {
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================

    @Override
    public BufferedImage transform(BufferedImage src) {
        int width = (int) targetWidth;
        int height = (int) targetHeight;
        if (src.getWidth() == width && src.getHeight() == height)
            return src;

        Image tmp = src.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.drawImage(tmp, 0, 0, null);
        g.dispose();
        return resized;
    }
}
