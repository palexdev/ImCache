package io.github.palexdev.imcache.core;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

import io.github.palexdev.imcache.cache.ICache;
import io.github.palexdev.imcache.cache.MemoryCache;
import io.github.palexdev.imcache.utils.URLHandler;

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
    public ImRequest request(URL url) {
        return new ImRequest(url);
    }

    public ImRequest request(String s) {
        return request(URLHandler.toURL(s).orElse(null));
    }

    public ImRequest request(File file) {
        return request(URLHandler.toURL(file.getAbsolutePath()).orElse(null));
    }

    public ImRequest request(Path path) {
        return request(path.toFile());
    }

    protected void store(ImRequest request, ImImage src, ImImage out) {
        String id = request.id();
        ImImage toSave = storeStrategy == StoreStrategy.SAVE_ORIGINAL ? src : out;
        cache.store(id, toSave);
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