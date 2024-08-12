package io.github.palexdev.imcache.cache;

import io.github.palexdev.imcache.core.Image;

import java.util.Optional;

public class MemoryCache extends Cache<Image> {

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public void store(String name, Image img) {
        if (capacity == 0) return;
        if (size() == capacity) cache.pollFirstEntry();
        cache.put(name, img);
    }

    @Override
    public boolean contains(String name) {
        return cache.containsKey(name);
    }

    @Override
    public Optional<Image> get(String name) {
        return Optional.ofNullable(cache.get(name));
    }

    @Override
    public Optional<Image> getImage(String name) {
        return get(name);
    }

    @Override
    public boolean remove(String name) {
        return Optional.ofNullable(cache.remove(name)).isPresent();
    }
}
