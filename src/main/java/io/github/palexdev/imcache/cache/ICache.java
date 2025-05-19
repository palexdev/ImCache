package io.github.palexdev.imcache.cache;

import io.github.palexdev.imcache.core.ImImage;
import java.util.Map;
import java.util.Optional;

public interface ICache<V> extends Iterable<Map.Entry<String, V>> {

    default ICache<V> scan() {
        return this;
    }

    void store(String id, ImImage img);

    default void store(WithID id, ImImage img) {
        store(id.id(), img);
    }

    boolean contains(String id);

    default boolean contains(WithID id) {
        return contains(id.id());
    }

    Optional<V> get(String id);

    default Optional<V> get(WithID id) {
        return get(id.id());
    }

    Optional<ImImage> getImage(String id);

    default Optional<ImImage> getImage(WithID id) {
        return getImage(id.id());
    }

    boolean remove(String id);

    default boolean remove(WithID id) {
        return remove(id.id());
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
