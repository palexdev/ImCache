package io.github.palexdev.imcache.core;

import io.github.palexdev.imcache.cache.Identifiable;
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
import java.util.function.Function;

public class ImRequest implements Identifiable {
    //================================================================================
    // Properties
    //================================================================================
    private final ImCache cache;
    private RequestState state = RequestState.READY;
    private String id;
    private final URL url;
    private boolean overwrite = false;
    private final List<Transform> transforms = new ArrayList<>();
    private ThrowingConsumer<URLConnection> urlConfig = c -> {};
    private ThrowingConsumer<Result> onStateChanged = r -> {};
    private Function<BufferedImage, byte[]> imageConverter = i -> ImageUtils.toBytes("png", i);

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
               "state=" + state +
               ", url=" + url +
               '}';
    }

    //================================================================================
    // Methods
    //================================================================================

    // Execution
    public ImRequest execute() {
        // Result data
        RequestState state = RequestState.READY;
        ImImage src = null;
        ImImage out = null;
        Result result = new Result(this);
        AtomicBoolean cacheHit = new AtomicBoolean(false);
        try {
            if (url == null) {
                throw new ImCacheException("Could not execute request %s because url is null".formatted(this));
            }
            // Update and notify request started
            updateState(RequestState.STARTED, result);

            if (isOverwrite()) {
                src = ImImage.wrap(url, URLHandler.resolve(this));
            } else {
                src = OptionalWrapper.wrap(cache.storage().getImage(this))
                    .ifPresent(i -> cacheHit.set(true))
                    .orElseGet(() -> ImImage.wrap(url, URLHandler.resolve(this)));
            }
            out = transform(src);
            cache.store(this, src, out);

            // Determine specific success state, either cache hit or simple success
            // Then build the result
            state = cacheHit.get() ? RequestState.CACHE_HIT : RequestState.SUCCEEDED;
            result = new Result(this, src, out, null);
        } catch (Exception ex) {
            state = RequestState.FAILED;
            result = new Result(this, src, out, ex);
        } finally {
            // At the end of all operations update the state
            updateState(state, result);
        }
        return this;
    }

    public Future<ImRequest> executeAsync() {
        return AsyncUtils.runAsync(this::execute);
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

    public ImRequest onStateChanged(ThrowingConsumer<Result> onStateChanged) {
        this.onStateChanged = onStateChanged;
        return this;
    }

    public ImRequest setImageConverter(Function<BufferedImage, byte[]> imageConverter) {
        this.imageConverter = imageConverter;
        return this;
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public RequestState state() {
        return state;
    }

    public void updateState(RequestState state, Result result) {
        this.state = state;
        if (onStateChanged != null) {
            try {
                onStateChanged.accept(result);
            } catch (Exception ex) {
                throw new ImCacheException(
                    "Failed to execute onStateChanged callback for request %s"
                        .formatted(this),
                    ex
                );
            }
        }
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
        OptionalWrapper<ImImage> src,
        OptionalWrapper<ImImage> out,
        OptionalWrapper<Throwable> error
    ) {

        private Result(ImRequest request) {
            this(request, null, null, (Throwable) null);
        }

        public Result(ImRequest request, ImImage src, ImImage out, Throwable error) {
            this(request,
                OptionalWrapper.ofNullable(src),
                OptionalWrapper.ofNullable(out),
                OptionalWrapper.ofNullable(error)
            );
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

        public RequestState state() {
            return request.state();
        }

        public boolean isEmpty() {
            return src == null || out == null;
        }
    }
}
