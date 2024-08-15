package io.github.palexdev.imcache.core;

import io.github.palexdev.imcache.cache.ICache;
import io.github.palexdev.imcache.cache.MemoryCache;

import java.util.Optional;
import java.util.function.Supplier;

public class ImCache {
    //================================================================================
    // Singleton
    //================================================================================
    private static final ImCache instance = new ImCache();

    public static ImCache instance() {
        return instance;
    }

    //================================================================================
    // Properties
    //================================================================================
    private ICache<?> cache = new MemoryCache();
    private StoreStrategy storeStrategy = StoreStrategy.SAVE_ORIGINAL;

    //================================================================================
    // Constructors
    //================================================================================
    private ImCache() {}

    //================================================================================
    // Methods
    //================================================================================

    // Execution
    public Request request() {
        return new Request();
    }

    // TODO should we cache the request too?
    //      in that case there is no need to clean up when changing save path
    protected void store(Request request, Image src, Image out) {
        String name = cache.toName(request);
        Image toSave = storeStrategy == StoreStrategy.SAVE_ORIGINAL ? src : out;
        cache.store(name, toSave);

    }

    public void clear() {
        Optional.ofNullable(cache).ifPresent(ICache::clear);
    }

    // Setup
    public ImCache cacheConfig(Supplier<ICache<?>> config) {
        this.cache = config.get();
        return this;
    }

    public ImCache saveStrategy(StoreStrategy storeStrategy) {
        this.storeStrategy = storeStrategy;
        return this;
    }

    //================================================================================
    // Getters
    //================================================================================
    public ICache<?> storage() {
        return cache;
    }

    public StoreStrategy getSaveStrategy() {
        return storeStrategy;
    }

    //================================================================================
    // Inner Classes
    //================================================================================
    public enum StoreStrategy {
        SAVE_ORIGINAL,
        SAVE_TRANSFORMED,
    }
}