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

/// The [AspectRatioCrop] class is an implementation of the [Transform] interface that crops an image to a specified
/// target aspect ratio. The crop area is centered on the original image.
public class AspectRatioCrop implements Transform {
    //================================================================================
    // Properties
    //================================================================================
    private final int targetWidthRatio;
    private final int targetHeightRatio;

    //================================================================================
    // Constructors
    //================================================================================
    public AspectRatioCrop(int targetWidthRatio, int targetHeightRatio) {
        this.targetWidthRatio = targetWidthRatio;
        this.targetHeightRatio = targetHeightRatio;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public BufferedImage transform(BufferedImage src) {
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        double targetAspectRatio = (double) targetWidthRatio / targetHeightRatio;

        // Crop area
        int cropWidth, cropHeight;
        if ((double) srcWidth / srcHeight > targetAspectRatio) {
            // Image is wider than the target aspect ratio
            cropHeight = srcHeight;
            cropWidth = (int) (srcHeight * targetAspectRatio);
        } else {
            // Image is taller than the target aspect ratio
            cropWidth = srcWidth;
            cropHeight = (int) (srcWidth / targetAspectRatio);
        }
        int x = (srcWidth - cropWidth) / 2;
        int y = (srcHeight - cropHeight) / 2;

        // Perform crop
        BufferedImage cropped = src.getSubimage(x, y, cropWidth, cropHeight);
        BufferedImage out = new BufferedImage(cropped.getWidth(), cropped.getHeight(), src.getType());
        Graphics2D g = out.createGraphics();
        g.drawImage(cropped, 0, 0, null);
        g.dispose();
        return out;
    }
}
