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

/// Represents a single operation that transforms a source [BufferedImage]
/// into a modified [BufferedImage].
///
/// This interface defines a functional contract for implementing image
/// processing and transformation logic. Implementations are expected to provide
/// specific image transformation functionalities, such as resizing, cropping,
/// adding filters, rotation, and more.
@FunctionalInterface
public interface Transform {
    BufferedImage transform(BufferedImage src);
}
