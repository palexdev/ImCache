package io.github.palexdev.imcache.core;

import io.github.palexdev.imcache.cache.WithID;
import io.github.palexdev.imcache.exceptions.ImCacheException;
import io.github.palexdev.imcache.transforms.Transform;
import io.github.palexdev.imcache.utils.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

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
    // Overridden Methods
    //================================================================================

    public String id() {
        if (id == null) {
            try {
                id = UUID.nameUUIDFromBytes(url.toString().getBytes()).toString();
            } catch (Exception ex) {
                throw new ImCacheException(
                    "Failed to generate id for request %s to a name"
                        .formatted(this),
                    ex
                );
            }
        }
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
    // Methods
    //================================================================================

    // Execution
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
                // Determine specific success state, either cache hit or simply success
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

    public ImRequest execute() {
        return execute(null);
    }

    public Future<ImRequest> executeAsync(Consumer<Result> callback) {
        return AsyncUtils.runAsync(() -> execute(callback));
    }

    protected ImImage transform(ImImage src) {
        if (transforms.isEmpty()) return src;
        BufferedImage img = src.asImage();
        for (Transform transform : transforms) {
            img = transform.transform(img);
        }
        return ImImage.wrap(src.url(), imageConverter.apply(img));
    }

    // Setup
    public ImRequest transform(Transform transform) {
        transforms.add(transform);
        return this;
    }

    public ImRequest overwrite(boolean overwrite) {
        this.overwrite = overwrite;
        return this;
    }

    public ImRequest urlConfig(ThrowingConsumer<URLConnection> urlConfig) {
        this.urlConfig = urlConfig;
        return this;
    }

    public ImRequest setImageConverter(Function<BufferedImage, byte[]> imageConverter) {
        this.imageConverter = imageConverter;
        return this;
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public Result result() {
        return result;
    }

    public RequestState state() {
        return result.state();
    }

    public URL url() {
        return url;
    }

    public ThrowingConsumer<URLConnection> getUrlConfig() {
        return urlConfig;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    //================================================================================
    // Inner Classes
    //================================================================================
    public enum RequestState {
        READY,
        STARTED,
        FAILED,
        SUCCEEDED,
        CACHE_HIT,
    }

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

        public String id() {
            return request().id();
        }

        public ImImage unwrapSrc() {
            return src.optional().get();
        }

        public ImImage unwrapOut() {
            return out.optional().get();
        }

        public Throwable unwrapError() {
            return error.optional().get();
        }

        public boolean isSuccess() {
            return state == RequestState.SUCCEEDED || state == RequestState.CACHE_HIT;
        }

        public boolean isFailed() {
            return state == RequestState.FAILED;
        }

        public Result withState(RequestState state) {
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
