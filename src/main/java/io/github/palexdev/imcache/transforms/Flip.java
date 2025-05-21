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
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/// The [Flip] class is a transformation that flips a source [BufferedImage] either horizontally or vertically based on
/// the specified [FlipOrientation].
/// This transformation modifies the image by inverting its pixels along the specified axis.
public class Flip implements Transform {
    //================================================================================
    // Properties
    //================================================================================
    private final FlipOrientation orientation;

    //================================================================================
    // Constructors
    //================================================================================
    public Flip(FlipOrientation orientation) {
        this.orientation = orientation;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public BufferedImage transform(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage flipped = new BufferedImage(w, h, src.getType());
        Graphics2D g = flipped.createGraphics();

        double scaleX, scaleY, translateX, translateY;
        switch (orientation) {
            case HORIZONTAL -> {
                scaleX = -1.0;
                scaleY = 1.0;
                translateX = -w;
                translateY = 0.0;
            }
            case VERTICAL -> {
                scaleX = 1.0;
                scaleY = -1.0;
                translateX = 0.0;
                translateY = -h;
            }
            default -> throw new IllegalStateException("Unexpected value: " + orientation);
        }
        AffineTransform at = new AffineTransform();
        at.scale(scaleX, scaleY);
        at.translate(translateX, translateY);
        g.setTransform(at);
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return flipped;
    }

    //================================================================================
    // Internal Classes
    //================================================================================
    public enum FlipOrientation {
        HORIZONTAL,
        VERTICAL
    }
}
