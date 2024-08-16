package io.github.palexdev.imcache.cache;

import io.github.palexdev.imcache.core.Request;
import io.github.palexdev.imcache.core.Image;

import java.util.Optional;

public interface ICache<V> {

    default ICache<V> scan() {
        return this;
    }

    void store(String id, Image img);

    boolean contains(String id);

    default boolean contains(Request request) {
        return contains(request.id());
    }

    Optional<V> get(String id);

    default Optional<V> get(Request request) {
        return get(request.id());
    }

    Optional<Image> getImage(String id);

    default Optional<Image> getImage(Request request) {
        return getImage(request.id());
    }

    boolean remove(String id);

    default boolean remove(Request request) {
        return remove(request.id());
    }

    boolean removeOldest();

    void clear();

    int size();

    int getCapacity();

    ICache<V> setCapacity(int capacity);
}
