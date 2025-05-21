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

import io.github.palexdev.imcache.exceptions.TransformException;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;

/// The [CircleCrop] class implements a circular cropping image transformation.
///
/// It takes a square image and masks it into a circle, optionally filling the background
/// and outlining the circle with a customizable stroke. This transformation ensures
/// high-quality rendering using antialiasing and rendering hints.
///
///
/// **Note:** The input image must be square; otherwise, a [TransformException]
/// is thrown.
public class CircleCrop implements Transform {
    //================================================================================
    // Properties
    //================================================================================
    private final Color bgColor;
    private final Color strokeColor;
    private final float strokeWidth;

    //================================================================================
    // Constructors
    //================================================================================
    public CircleCrop(Color bgColor, Color strokeColor, float strokeWidth) {
        this.bgColor = bgColor;
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public BufferedImage transform(BufferedImage src) {
        if (src.getWidth() != src.getHeight()) {
            throw new TransformException("Width should be equal to height");
        }

        int size = src.getWidth();
        BufferedImage circle = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = circle.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g.setPaint(bgColor);
        g.fillRect(0, 0, size, size);

        Arc2D arc = new Arc2D.Float(0f, 0f, size, size, 0f, -360f, Arc2D.OPEN);
        g.setClip(arc);
        g.drawImage(src, 0, 0, size, size, null);

        if (strokeColor != null) {
            g.setColor(strokeColor);
            g.setStroke(new BasicStroke(strokeWidth));
            g.draw(arc);
        }
        g.dispose();
        return circle;
    }
}
