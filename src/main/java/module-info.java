module ImCache {
    //***** Dependencies *****//
    requires java.desktop;

    //***** Exports *****//
    // Cache Package
    exports io.github.palexdev.imcache.cache;

    // Core Package
    exports io.github.palexdev.imcache.core;

    // Exceptions Package
    exports io.github.palexdev.imcache.exceptions;

    // Network Package
    exports io.github.palexdev.imcache.network;

    // Transforms Package
    exports io.github.palexdev.imcache.transforms;

    // Utils Package
    exports io.github.palexdev.imcache.utils;
}