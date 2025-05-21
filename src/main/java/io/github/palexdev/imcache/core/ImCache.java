package io.github.palexdev.imcache.core;

import io.github.palexdev.imcache.cache.*;
import io.github.palexdev.imcache.core.ImRequest.RequestState;
import io.github.palexdev.imcache.transforms.Transform;
import io.github.palexdev.imcache.utils.URLHandler;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.function.Supplier;

/// Core class which eases communication between the request system ([ImRequest]) and the caching system ([ImgCache]).
///
/// A single instance of this class should cover most use cases, that's why it offers a 'default' instance via
/// [#instance()]; however, the no-args constructor is left public in case one needs separate caches.
///
/// A request is generated starting from the source [URL] for a certain resource, which is the most generic way to
/// represent a resource; it can be a network resource, a file or something else. Once a request's execution successfully
/// terminates, it asks the associated [ImCache] instance to save the result, see [#store(ImRequest, ImImage, ImImage)].
///
/// While `removals` are delegated directly to the [Cache], `get` operations should be done by creating a new request.
/// A resource may or may not be cached anymore at a given time. [ImRequest] uses [RequestState#CACHE_HIT] to indicate
/// that the resource was found in the cache, and [RequestState#SUCCEEDED] to indicate that the resource was loaded from
/// its [URL].
/// Of course, since [ImCache] allows getting the [ImgCache] instance with [#storage()], you can perform operations directly
/// on the cache data structure, although it's not recommended unless you really need it for your use case.
///
/// The last core concept is [Transforms][Transform]. `ImCache` does not provide only a way to load and cache images, but
/// also a way to modify them via the [Transform#transform(BufferedImage)] API. Transformations are applied by [ImRequest]
/// after the original resource is loaded. At that point we can store the results, but this raises a question: which one
/// do you want to save? `ImCache` allows you to control this behavior by setting the [StoreStrategy],
/// see [#store(ImRequest, ImImage, ImImage)]. /// The reason the system doesn't allow caching both is to save space and for simplicity
/// Most of the time you probably want to cache the original resource, as transform operations are relatively fast.
///
/// _Defaults & Config_
/// - By default, the store strategy is set to [StoreStrategy#SAVE_ORIGINAL], can be changed via [#storeStrategy(StoreStrategy)]
/// - By default, this uses an in-memory cache ([MemoryCache]), can be changed via [#cacheConfig(Supplier)]
///
/// @see ImgCache
/// @see MemoryCache
/// @see DiskCache
public class ImCache {
    //================================================================================
    // Default Instance
    //================================================================================
    private static final ImCache instance = new ImCache();

    public static ImCache instance() {
        return instance;
    }

    //================================================================================
    // Properties
    //================================================================================
    private ImgCache<?> cache = new MemoryCache();
    private StoreStrategy storeStrategy = StoreStrategy.SAVE_ORIGINAL;

    //================================================================================
    // Methods
    //================================================================================

    // Request

    /// Creates a new [ImRequest] with the given [URL].
    public ImRequest request(URL url) {
        return new ImRequest(this, url);
    }

    /// Creates a new [ImRequest] by converting the provided string to a [URL] with [URLHandler#toURL(String)].
    ///
    /// If the conversion fails, the request will have a null [URL].
    public ImRequest request(String s) {
        return request(URLHandler.toURL(s).orElse(null));
    }

    /// Creates a new [ImRequest] by converting the provided [File] to a [URL] with [URLHandler#toURL(File)].
    ///
    /// If the conversion fails, the request will have a null [URL].
    public ImRequest request(File file) {
        return request(URLHandler.toURL(file).orElse(null));
    }

    /// Delegates to [#request(File)].
    public ImRequest request(Path path) {
        return request(path.toFile());
    }

    /// Stores a completed request in the cache by its [ImRequest#id()]. The image to be saved is either the original
    /// or the transformed one, depending on the [StoreStrategy].
    ///
    /// Note that if no transforms were applied, the `src` and the `out` parameters will be the same. In other words,
    /// no matter the strategy, the original will be saved.
    ///
    /// If the image to be saved is `null`, nothing happens.
    protected void store(ImRequest request, ImImage src, ImImage out) {
        String id = request.id();
        ImImage toSave = storeStrategy == StoreStrategy.SAVE_ORIGINAL ? src : out;
        if (toSave != null) cache.store(id, toSave);
    }

    // Removal

    /// Delegates to [ImgCache#remove(String)].
    public boolean remove(String id) {
        return cache.remove(id);
    }

    /// Delegates to [ImCache#remove(WithID)].
    public boolean remove(WithID id) {
        return cache.remove(id);
    }

    /// Delegates to [ImgCache#clear()].
    public ImCache clear() {
        cache.clear();
        return this;
    }

    // Setup

    /// Replaces the current cache with a new one generated by the given config supplier.
    ///
    /// Note that the current cache is not cleared automatically.
    public ImCache cacheConfig(Supplier<ImgCache<?>> config) {
        this.cache = config.get();
        return this;
    }

    /// Sets the [StoreStrategy] to be used when saving the result of a request.
    ///
    /// @see #store(ImRequest, ImImage, ImImage)
    public ImCache setStoreStrategy(StoreStrategy storeStrategy) {
        this.storeStrategy = storeStrategy;
        return this;
    }

    //================================================================================
    // Getters
    //================================================================================

    /// @return the [ImgCache] instance used by this [ImCache] to store and retrieve images
    public ImgCache<?> storage() {
        return cache;
    }

    /// @return the [StoreStrategy] used by this [ImCache] to determine which image to save when a request completes
    /// successfully
    /// @see #store(ImRequest, ImImage, ImImage)
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