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

/// The [Pad] class implements a padding transformation for images.
///
/// It resizes an input image to fit within the target dimensions while preserving the aspect ratio and centers it on a
/// new canvas filled with a specified padding color. This ensures that the final image has the exact target dimensions
/// without cropping or stretching the original content.
public class Pad implements Transform {
    //================================================================================
    // Properties
    //================================================================================
    private final double targetWidth;
    private final double targetHeight;
    private final Color paddingColor;

    //================================================================================
    // Constructors
    //================================================================================
    public Pad(double targetWidth, double targetHeight) {
        this(targetWidth, targetHeight, Color.WHITE);
    }

    public Pad(double targetWidth, double targetHeight, Color paddingColor) {
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        this.paddingColor = paddingColor;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public BufferedImage transform(BufferedImage src) {
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int targetWidth = (int) this.targetWidth;
        int targetHeight = (int) this.targetHeight;

        BufferedImage padded = new BufferedImage(targetWidth, targetHeight, src.getType());
        Graphics2D g = padded.createGraphics();
        g.setColor(paddingColor);
        g.fillRect(0, 0, targetWidth, targetHeight);

        double wRatio = (double) targetWidth / srcWidth;
        double hRatio = (double) targetHeight / srcHeight;
        double scale = Math.min(wRatio, hRatio);

        int newWidth = (int) (srcWidth * scale);
        int newHeight = (int) (srcHeight * scale);

        int x = (targetWidth - newWidth) / 2;
        int y = (targetHeight - newHeight) / 2;

        g.drawImage(src, x, y, newWidth, newHeight, null);
        g.dispose();
        return padded;
    }
}
