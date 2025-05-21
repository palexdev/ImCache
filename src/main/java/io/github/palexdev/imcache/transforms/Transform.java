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
