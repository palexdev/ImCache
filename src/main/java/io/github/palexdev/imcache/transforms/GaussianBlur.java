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
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

/// The [GaussianBlur] class applies a Gaussian blur filter to a given [BufferedImage]. The transformation uses a
/// convolution operation with a pre-defined Gaussian kernel.
///
/// The kernel used in this implementation is a 5x5 matrix that approximates the Gaussian distribution.
public class GaussianBlur implements Transform {

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public BufferedImage transform(BufferedImage src) {
        float[] matrix = {
            1f / 256, 4f / 256, 6f / 256, 4f / 256, 1f / 256,
            4f / 256, 16f / 256, 24f / 256, 16f / 256, 4f / 256,
            6f / 256, 24f / 256, 36f / 256, 24f / 256, 6f / 256,
            4f / 256, 16f / 256, 24f / 256, 16f / 256, 4f / 256,
            1f / 256, 4f / 256, 6f / 256, 4f / 256, 1f / 256
        };
        Kernel kernel = new Kernel(5, 5, matrix);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return op.filter(src, null);
    }
}
