/*
 * Copyright (C) 2025 Parisi Alessandro - alessandro.parisi406@gmail.com
 * This file is part of ImCache (https://github.com/palexdev/imcache)
 *
 * ImCache is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * ImCache is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ImCache. If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.palexdev.imcache.core;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.imageio.ImageIO;

import io.github.palexdev.imcache.cache.WithID;
import io.github.palexdev.imcache.exceptions.ImCacheException;
import io.github.palexdev.imcache.transforms.Transform;
import io.github.palexdev.imcache.utils.*;

/// As the name suggests, this class represents a request for loading a certain image from a [URL].
///
/// The request is stateful. Both the state and the result can be retrieved by using [#result()] and observed by giving
/// a callback to the execution, see [#execute(Consumer<Result>)] and [#executeAsync(Consumer<Result>)].
///
/// A resource can either be fetched from its source [URL] or retrieved from the cache if present. This class implements
/// [WithID], and the id is generated from the [URL] (see [WithID#generateId(URL)]).
/// Requests that load a certain resource from the same [URL] point to the same resource in the cache.
///
/// The request is also responsible for applying any desired [Transform] to the loaded image. They are added one after
/// the other by [#transform(Transform)] and applied on the image at the end of the loading process and before asking
/// [ImCache] to store the result (see [ImCache#store(ImRequest, ImImage, ImImage)]).
/// In the case of multiple transforms, the result of the previous transform is passed as the input of the next one.
///
/// [Transforms][Transform] are generally not much expensive, however, keep in mind that to apply them, we must first
/// convert the loaded image raw data to a [BufferedImage] and then back to a byte array because that's the format used
/// by [ImImage]. The conversions are handled by the [ImageIO] library, and by default, the format used to convert a
/// [BufferedImage] back to a byte array is `png`. This can be changed by setting the converting function via
/// [#setImageConverter(Function)].
///
/// _Other features and configs_
///
/// - The request can be executed on the same thread as the callee or asynchronously via [#executeAsync(Consumer<Result>)].
/// - To load the resource a [URLConnection] is created on its [URL] by the [URLHandler]. The connection can be configured
/// via [#urlConfig(ThrowingConsumer)].
/// - By default, if a resource is present in the cache, it will be fetched from there and complete the execution with the
/// [RequestState#CACHE_HIT] state. You can force the request to always fetch the resource from its [URL] without necessarily
/// removing it from the cache by setting [#overwrite(boolean)] to `true`.
///
/// @see RequestState
public class ImRequest implements WithID {
    //================================================================================
    // Properties
    //================================================================================
    private final ImCache cache;
    private String id;
    private final URL url;
    private boolean overwrite = false;
    private final List<Transform> transforms = new ArrayList<>();
    private ThrowingConsumer<URLConnection> urlConfig = c -> {};
    private Function<BufferedImage, byte[]> imageConverter = i -> ImageUtils.toBytes("png", i);

    private Result result = new Result(this);

    //================================================================================
    // Constructors
    //================================================================================
    public ImRequest(ImCache cache, URL url) {
        this.cache = cache;
        this.url = url;
    }

    //================================================================================
    // Methods
    //================================================================================

    // Execution

    /// Core method responsible for fetching an image either from its source [URL] or from the cache if present
    /// (unless [#overwrite(boolean)] was set to `true`).
    ///
    /// Once the image is loaded, it is transformed by the [#transform(ImImage)] method, and finally, both the original and
    /// the output are sent to [ImCache#store(ImRequest, ImImage, ImImage)] for caching.
    ///
    /// The given callback is called every time the request state changes, see [RequestState] for more information on the
    /// various states.
    public ImRequest execute(Consumer<Result> callback) {
        result = new Result(this); // Reset result
        ImImage src = null;
        ImImage out = null;
        AtomicBoolean cacheHit = new AtomicBoolean(false);
        try {
            if (url == null) {
                throw new ImCacheException("Could not execute request %s because url is null".formatted(this));
            }
            // Update and notify request started
            result = result.withState(RequestState.STARTED);
            if (callback != null) callback.accept(result);

            if (isOverwrite()) {
                src = ImImage.wrap(url, URLHandler.resolve(this));
            } else {
                src = OptionalWrapper.wrap(cache.storage().getImage(this))
                    .ifPresent(i -> cacheHit.set(true))
                    .orElseGet(() -> ImImage.wrap(url, URLHandler.resolve(this)));
            }
            out = transform(src);
            cache.store(this, src, out);

            result = new Result(
                this,
                // Determine the specific success state, either cache hit or simply success
                cacheHit.get() ? RequestState.CACHE_HIT : RequestState.SUCCEEDED,
                src,
                out,
                null
            );
        } catch (Exception ex) {
            result = new Result(this, RequestState.FAILED, src, out, ex);
        } finally {
            if (callback != null) callback.accept(result);
        }
        return this;
    }

    /// Convenience method for executing the request without a callback.
    ///
    /// @see #execute(Consumer)
    public ImRequest execute() {
        return execute(null);
    }

    /// Executes the request asynchronously by using [AsyncUtils].
    ///
    /// @see #execute(Consumer)
    public Future<ImRequest> executeAsync(Consumer<Result> callback) {
        return AsyncUtils.runAsync(() -> execute(callback));
    }

    /// If any [Transforms][Transform] were added before the execution, they are applied on the given source image,
    /// otherwise returns the source image unchanged.
    ///
    /// Unfortunately, to make the system easier, to apply transformations, we first convert the source image raw data
    /// to a [BufferedImage] and at the end back to a byte array.
    ///
    /// @see ImageUtils#toImage(Object)
    /// @see #setImageConverter(Function)
    protected ImImage transform(ImImage src) {
        if (src == null) {
            throw new ImCacheException("Could not transform image because source image is null");
        }
        if (transforms.isEmpty()) return src;
        BufferedImage img = src.asImage();
        for (Transform transform : transforms) {
            img = transform.transform(img);
        }
        return ImImage.wrap(src.url(), imageConverter.apply(img));
    }

    // Setup

    /// Adds the given [Transform] to the request.
    public ImRequest transform(Transform transform) {
        transforms.add(transform);
        return this;
    }

    /// Sets whether the current request should overwrite the cached content.
    public ImRequest overwrite(boolean overwrite) {
        this.overwrite = overwrite;
        return this;
    }

    /// A consumer that allows you to configure the [URLConnection] used to fetch the resource from its [URL].
    public ImRequest urlConfig(ThrowingConsumer<URLConnection> urlConfig) {
        this.urlConfig = urlConfig;
        return this;
    }

    /// This function is used to convert a [BufferedImage] back to a byte array after applying all the transformations.
    ///
    /// By default, we use [ImageUtils#toBytes(String, Object)] with `png` as the format.
    public ImRequest setImageConverter(Function<BufferedImage, byte[]> imageConverter) {
        this.imageConverter = imageConverter;
        return this;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================

    /// @return an id generated from the resource's [URL]
    /// @see WithID#generateId(URL)
    @Override
    public String id() {
        if (id == null) id = WithID.generateId(url);
        return id;
    }

    @Override
    public String toString() {
        return "ImRequest{" +
               "id='" + id + '\'' +
               ", url=" + url +
               ", result=" + result +
               '}';
    }

    //================================================================================
    // Getters/Setters
    //================================================================================

    /// @return the request's result, which includes the state, the source image, the transformed image, and any error.
    public Result result() {
        return result;
    }

    /// Convenience method for accessing the request's state from its result.
    public RequestState state() {
        return result.state();
    }

    /// The [URL] at which the resource can be found.
    public URL url() {
        return url;
    }

    /// @see [#urlConfig(ThrowingConsumer)]
    public ThrowingConsumer<URLConnection> getUrlConfig() {
        return urlConfig;
    }

    /// @see [#overwrite(boolean)]
    public boolean isOverwrite() {
        return overwrite;
    }

    //================================================================================
    // Inner Classes
    //================================================================================

    /// Enumeration representing the various states of a request.
    public enum RequestState {
        /// The request is ready to be executed, this is the first stage.
        READY,

        /// The request is being worked on.
        STARTED,

        /// The request has failed for some reason. If an exception was captured, it is available in its result via
        /// [Result#error()] or [Result#unwrapError()].
        FAILED,

        /// The request has succeeded. Both the source and the transformed image are available in its result.
        SUCCEEDED,

        /// The request has succeeded, but the resource was found in the cache. No connection to its [URL] was made.
        CACHE_HIT,
    }

    /// Wrapper class to represent the result of a request. Being immutable, it changes as the execution advances.
    ///
    /// Depending on the state, the data contained here may or may not be available. That's why we use [Optional].
    /// However, if you query the state via [#state()], you can make some fair assumptions. For example, if the state is
    /// [RequestState#FAILED] then no image is available, but an exception may be, and it should be safe to get it directly
    /// by unwrapping the [Optional] with [#unwrapError()]. The same login applies to [RequestState#SUCCEEDED] or
    /// [RequestState#CACHE_HIT].
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public record Result(
        ImRequest request,
        RequestState state,
        OptionalWrapper<ImImage> src,
        OptionalWrapper<ImImage> out,
        OptionalWrapper<Throwable> error
    ) {

        private Result(ImRequest request) {
            this(request, RequestState.READY, null, null, (Throwable) null);
        }

        public Result(ImRequest request, RequestState state, ImImage src, ImImage out, Throwable error) {
            this(request, state,
                OptionalWrapper.ofNullable(src),
                OptionalWrapper.ofNullable(out),
                OptionalWrapper.ofNullable(error)
            );
        }

        /// @return the request's id, given by [ImRequest#id()].
        public String id() {
            return request().id();
        }

        /// Convenience method for accessing the source image directly.
        public ImImage unwrapSrc() {
            return src.optional().get();
        }

        /// Convenience method for accessing the transformed image directly.
        public ImImage unwrapOut() {
            return out.optional().get();
        }

        /// Convenience method for accessing the error directly.
        public Throwable unwrapError() {
            return error.optional().get();
        }

        /// Convenience method to check if the request has succeeded either with [RequestState#SUCCEEDED] or
        /// [RequestState#CACHE_HIT].
        public boolean isSuccess() {
            return state == RequestState.SUCCEEDED || state == RequestState.CACHE_HIT;
        }

        /// Convenience method to check if the request has failed.
        public boolean isFailed() {
            return state == RequestState.FAILED;
        }

        Result withState(RequestState state) {
            return new Result(request, state, src, out, error);
        }

        @Override
        public String toString() {
            return "Result{" +
                   "state=" + state +
                   ", src=" + src +
                   ", out=" + out +
                   '}';
        }
    }
}
