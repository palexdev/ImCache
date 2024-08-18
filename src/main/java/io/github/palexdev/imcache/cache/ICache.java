package io.github.palexdev.imcache.cache;

import io.github.palexdev.imcache.core.ImImage;
import io.github.palexdev.imcache.core.ImRequest;

import java.util.Map;
import java.util.Optional;

public interface ICache<V> extends Iterable<Map.Entry<String, V>> {

    default ICache<V> scan() {
        return this;
    }

    void store(String id, ImImage img);

    boolean contains(String id);

    default boolean contains(ImRequest request) {
        return contains(request.id());
    }

    Optional<V> get(String id);

    default Optional<V> get(ImRequest request) {
        return get(request.id());
    }

    Optional<ImImage> getImage(String id);

    default Optional<ImImage> getImage(ImRequest request) {
        return getImage(request.id());
    }

    boolean remove(String id);

    default boolean remove(ImRequest request) {
        return remove(request.id());
    }

    boolean removeOldest();

    void clear();

    int size();

    int getCapacity();

    ICache<V> setCapacity(int capacity);

    default Map<String, V> asMap() {
        return Map.of();
    }
}
