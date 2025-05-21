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

import java.awt.image.BufferedImage;

/// The [CenterCrop] class implements a center-cropping image transformation.
///
/// It crops an input image to the desired width and height while maintaining the aspect ratio.
/// The crop is centered in the original image. After cropping, the result is resized
/// to exactly match the specified target dimensions.
public class CenterCrop implements Transform {
    //================================================================================
    // Properties
    //================================================================================
    private final double targetWidth;
    private final double targetHeight;

    //================================================================================
    // Constructors
    //================================================================================
    public CenterCrop(double targetWidth, double targetHeight) {
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public BufferedImage transform(BufferedImage src) {
        double sourceWidth = src.getWidth();
        double sourceHeight = src.getHeight();
        if (sourceWidth == targetWidth && sourceHeight == targetHeight)
            return src;

        double xScale = targetWidth / sourceWidth;
        double yScale = targetHeight / sourceHeight;
        double newXScale, newYScale;
        if (xScale < yScale) {
            newXScale = (1.0 / yScale) * xScale;
            newYScale = 1.0;
        } else {
            newXScale = 1.0;
            newYScale = (1.0 / xScale) * yScale;
        }

        double scaledWidth = sourceWidth * newXScale;
        double scaledHeight = sourceHeight * newYScale;
        double left = (sourceWidth - scaledWidth) / 2;
        double top = (sourceHeight - scaledHeight) / 2;
        BufferedImage centered = src.getSubimage(
            (int) left,
            (int) top,
            (int) scaledWidth,
            (int) scaledHeight
        );
        return new Resize(targetWidth, targetHeight).transform(centered);
    }
}
