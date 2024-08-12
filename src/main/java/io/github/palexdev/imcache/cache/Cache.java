package io.github.palexdev.imcache.cache;

import java.util.*;

public abstract class Cache<V> implements ICache<V> {
    //================================================================================
    // Properties
    //================================================================================
    protected final SequencedMap<String, V> cache;
    protected int capacity = 10;

    //================================================================================
    // Constructors
    //================================================================================
    public Cache() {
        this.cache = new LinkedHashMap<>();
    }

    protected Cache(SequencedMap<String, V> cache) {
        this.cache = cache;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public int size() {
        return cache.size();
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public ICache<V> setCapacity(int capacity) {
        while (size() > capacity) cache.pollFirstEntry();
        this.capacity = capacity;
        return this;
    }
}
