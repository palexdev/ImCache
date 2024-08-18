package io.github.palexdev.imcache.transforms;

import java.awt.image.BufferedImage;

@FunctionalInterface
public interface Transform {
    BufferedImage transform(BufferedImage src);
}
