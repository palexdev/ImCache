package io.github.palexdev.imcache.cache;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/// Generic cache API to store items by a unique string key, often referred to as 'id' (see also [WithID]).
///
/// Features:
/// 1) Allows checking, getting, storing and removing entries
/// 2) Allows removing the oldest entry
/// 3) Can be limited to a certain capacity beyond which oldest entries are removed
///
/// @param <V> the type of data stored by the cache
public interface Cache<V> {

    /// Default cache path: '${home}/.imcache'
    Path DEFAULT_CACHE_PATH = Path.of(System.getProperty("user.home"), ".imcache");

    /// Default cache capacity: 100 entries
    int DEFAULT_CAPACITY = 100;

    boolean contains(String id);

    /// Delegates to [#contains(String)]
    default boolean contains(WithID id) {
        return contains(id.id());
    }

    /// @return the cached value mapped to the given id as an [Optional]
    Optional<V> get(String id);

    /// Delegates to [#get(String)]
    default Optional<V> get(WithID id) {
        return get(id.id());
    }

    void store(String id, V value);

    /// Delegates to [#store(String, V)]
    default void store(WithID id, V value) {
        store(id.id(), value);
    }

    boolean remove(String id);

    /// Delegates to [#remove(String)]
    default boolean remove(WithID id) {
        return remove(id.id());
    }

    /// Implementations should define the logic to remove the oldest entry from the cache
    boolean removeOldest();

    /// Clears the cache
    void clear();

    /// @return the size of cache's backing data structure
    int size();

    /// @return whether the cache is empty
    default boolean isEmpty() {
        return size() == 0;
    }

    /// @return the maximum allowed number of entries in the cache
    int getCapacity();

    /// Sets the maximum allowed number of entries in the cache.
    ///
    /// If the capacity shrinks, implementations should also delete enough older entries to reach the new capacity.
    Cache<V> setCapacity(int capacity);

    /// @return the cache as a [Map]
    default Map<String, V> asMap() {
        return Map.of();
    }
}
