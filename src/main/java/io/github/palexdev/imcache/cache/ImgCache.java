package io.github.palexdev.imcache.cache;

import io.github.palexdev.imcache.core.ImImage;
import java.nio.file.Path;
import java.util.*;

public abstract class ImgCache<V> implements Cache<V>, Iterable<Map.Entry<String, V>> {
    //================================================================================
    // Properties
    //================================================================================
    protected final SequencedMap<String, V> cache;
    protected int capacity = 10;

    //================================================================================
    // Constructors
    //================================================================================
    public ImgCache() {
        this.cache = new LinkedHashMap<>();
    }

    protected ImgCache(SequencedMap<String, V> cache) {
        this.cache = cache;
    }

    //================================================================================
    // Abstract Methods
    //================================================================================

    public abstract ImgCache<V> scan(Path scanPath);

    public abstract void store(String id, ImImage img);

    public abstract Optional<ImImage> getImage(String id);

    //================================================================================
    // Methods
    //================================================================================

    public ImgCache<V> scan() {
        return scan(DEFAULT_CACHE_PATH);
    }

    public void store(WithID id, ImImage img) {
        store(id.id(), img);
    }

    public Optional<ImImage> getImage(WithID id) {
        return getImage(id.id());
    }

    public void forEach(BiConsumer<String, V> consumer) {
        cache.forEach(consumer);
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public Iterator<Map.Entry<String, V>> iterator() {
        return asMap().entrySet().iterator();
    }

    @Override
    public Optional<V> get(String id) {
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public void store(String id, V value) {
        if (capacity == 0) return;
        if (size() == capacity) removeOldest();
        cache.put(id, value);
    }

    @Override
    public boolean remove(String id) {
        return Optional.ofNullable(cache.remove(id)).isPresent();
    }

    @Override
    public boolean removeOldest() {
        if (cache.isEmpty()) return false;
        return remove(cache.firstEntry().getKey());
    }

    @Override
    public boolean contains(String id) {
        return cache.containsKey(id);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public Map<String, V> asMap() {
        return Collections.unmodifiableMap(cache);
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public Cache<V> setCapacity(int capacity) {
        while (size() > capacity) removeOldest();
        this.capacity = capacity;
        return this;
    }
}
