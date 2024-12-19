package io.github.palexdev.imcache.cache;

import java.util.Map;
import java.util.Optional;

import io.github.palexdev.imcache.core.ImImage;

public interface ICache<V> extends Iterable<Map.Entry<String, V>> {

    default ICache<V> scan() {
        return this;
    }

    void store(String id, ImImage img);

    default void store(Identifiable identifiable, ImImage img) {
        store(identifiable.id(), img);
    }

    boolean contains(String id);

    default boolean contains(Identifiable identifiable) {
        return contains(identifiable.id());
    }

    Optional<V> get(String id);

    default Optional<V> get(Identifiable identifiable) {
        return get(identifiable.id());
    }

    Optional<ImImage> getImage(String id);

    default Optional<ImImage> getImage(Identifiable identifiable) {
        return getImage(identifiable.id());
    }

    boolean remove(String id);

    default boolean remove(Identifiable identifiable) {
        return remove(identifiable.id());
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
