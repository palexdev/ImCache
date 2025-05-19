package io.github.palexdev.imcache.cache;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public interface Cache<V> {
    Path DEFAULT_CACHE_PATH = Path.of(System.getProperty("user.home"), ".imcache");

    default Cache<V> scan(Path scanPath) {
        return this;
    }

    boolean contains(String id);

    default boolean contains(WithID id) {
        return contains(id.id());
    }

    Optional<V> get(String id);

    default Optional<V> get(WithID id) {
        return get(id.id());
    }

    void store(String id, V value);

    default void store(WithID id, V value) {
        store(id.id(), value);
    }

    boolean remove(String id);

    default boolean remove(WithID id) {
        return remove(id.id());
    }

    boolean removeOldest();

    void clear();

    int size();

    int getCapacity();

    Cache<V> setCapacity(int capacity);

    default Map<String, V> asMap() {
        return Map.of();
    }
}
