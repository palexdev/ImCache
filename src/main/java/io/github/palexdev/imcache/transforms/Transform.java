package io.github.palexdev.imcache.transforms;

import io.github.palexdev.imcache.core.ImImage;

@FunctionalInterface
public interface Transform {
    ImImage transform(ImImage src);
}
