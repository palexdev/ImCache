package io.github.palexdev.imcache.cache;

import io.github.palexdev.imcache.core.Request;
import io.github.palexdev.imcache.exceptions.ImCacheException;
import io.github.palexdev.imcache.core.Image;

import java.util.Optional;
import java.util.UUID;

public interface ICache<V> {

    default String toName(Request request) {
        try {
            return UUID.nameUUIDFromBytes(
                request.url()
                    .toString()
                    .getBytes()
            ).toString();
        } catch (Exception ex) {
            throw new ImCacheException(
                "Failed to convert request %s to a name"
                    .formatted(request),
                ex
            );
        }
    }

    void store(String name, Image img);

    boolean contains(String name);

    default boolean contains(Request request) {
        return contains(toName(request));
    }

    Optional<V> get(String name);

    default Optional<V> get(Request request) {
        return get(toName(request));
    }

    Optional<Image> getImage(String name);

    default Optional<Image> getImage(Request request) {
        return getImage(toName(request));
    }

    boolean remove(String name);

    default boolean remove(Request request) {
        return remove(toName(request));
    }

    void clear();

    int size();

    int getCapacity();

    ICache<V> setCapacity(int capacity);
}
