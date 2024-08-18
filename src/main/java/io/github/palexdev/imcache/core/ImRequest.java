package io.github.palexdev.imcache.core;

import io.github.palexdev.imcache.exceptions.ImCacheException;
import io.github.palexdev.imcache.network.Downloader;
import io.github.palexdev.imcache.utils.AsyncUtils;
import io.github.palexdev.imcache.utils.OptionalWrapper;
import io.github.palexdev.imcache.utils.ThrowingConsumer;
import io.github.palexdev.imcache.utils.ThrowingTriConsumer;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;
import java.util.UUID;

public class ImRequest {
    //================================================================================
    // Properties
    //================================================================================
    private RequestState state = RequestState.READY;
    private String id;
    private URL url;
    private ThrowingConsumer<URLConnection> netConfig = c -> {};
    private ThrowingTriConsumer<Request, Image, Image> onSuccess = (r, src, out) -> {};
    private ThrowingConsumer<Request> onFail = r -> {};
    private ThrowingTriConsumer<ImRequest, ImImage, ImImage> onSuccess = (r, src, out) -> {};
    private ThrowingConsumer<ImRequest> onFail = r -> {};
    private boolean overwrite = false;

    //================================================================================
    // Constructors
    //================================================================================
    ImRequest() {}

    //================================================================================
    // Overridden Methods
    //================================================================================

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
        try {
            assert url != null;
            state = RequestState.STARTED;

            ImImage src;
            if (isOverwrite()) {
                src = ImImage.wrap(url, Downloader.download(this));
            } else {
                src = OptionalWrapper.wrap(ImCache.instance().storage().getImage(this))
                        .ifPresent(i -> state = RequestState.CACHE_HIT)
                        .orElseGet(() -> ImImage.wrap(url, Downloader.download(this)));
            }
            ImImage out = transform(src);
            ImCache.instance().store(this, src, out);

            if (state != RequestState.CACHE_HIT) state = RequestState.SUCCEEDED;
            succeeded(src, out);
        } catch (Exception ex) {
            state = RequestState.FAILED;
            failed(ex);
        }
        return this;
    }

    public ImRequest executeAsync() {
        AsyncUtils.runAsync(this::execute);
        return this;
    }

    protected ImImage transform(ImImage src) {
        // TODO implement
        return src;
    }

    protected void succeeded(ImImage src, ImImage out) {
        Optional.ofNullable(onSuccess).ifPresent(c -> {
            try {
                c.accept(this, src, out);
            } catch (Exception ex) {
                throw new ImCacheException(
                    "Failed to execute onSuccess action for request %s"
                        .formatted(this),
                    ex
                );
            }
        });
    }

    protected void failed(Throwable t) {
        Optional.ofNullable(onFail).ifPresent(c -> {
            try {
                c.accept(this);
            } catch (Exception ex) {
                throw new ImCacheException(
                    "Failed to execute onFail action for request %s"
                        .formatted(this),
                    ex
                );
            }
        });
        t.printStackTrace();
    }

    // Setup

    public ImRequest load(URL url) {
        this.url = url;
        return this;
    }

    public ImRequest load(URI uri) {
        try {
            load(uri.toURL());
        } catch (Exception ex) {
            throw new ImCacheException(
                "Load operation failed: could not convert %s to an URL"
                    .formatted(uri),
                ex
            );
        }
        return this;
    }

    public ImRequest load(String s) {
        return load(URI.create(s));
    }

    public ImRequest netConfig(ThrowingConsumer<URLConnection> netConfig) {
        this.netConfig = netConfig;
        return this;
    }

    public ImRequest onSuccess(ThrowingTriConsumer<ImRequest, ImImage, ImImage> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public ImRequest onFail(ThrowingConsumer<ImRequest> onFail) {
        this.onFail = onFail;
        return this;
    }

    public ImRequest overwrite(boolean overwrite) {
        this.overwrite = overwrite;
        return this;
    }

    //================================================================================
    // Getters
    //================================================================================
    public RequestState state() {
        return state;
    }

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

    public URL url() {
        return url;
    }

    public ThrowingConsumer<URLConnection> getNetConfig() {
        return netConfig;
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
}