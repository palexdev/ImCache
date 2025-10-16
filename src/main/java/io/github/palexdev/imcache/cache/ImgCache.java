/*
 * Copyright (C) 2025 Parisi Alessandro - alessandro.parisi406@gmail.com
 * This file is part of ImCache (https://github.com/palexdev/imcache)
 *
 * ImCache is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * ImCache is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ImCache. If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.palexdev.imcache.cache;

import io.github.palexdev.imcache.core.ImImage;
import io.github.palexdev.imcache.utils.ImageUtils;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;

/// Abstract specialization of [Cache] which implements common functionalities such as:
/// - the backing data structure, which is a [LinkedHashMap] by default
/// - the cache's capacity
/// - common operations: contains, get, store, remove
/// - additional methods to work with [ImImage] objects
///
/// Items are stored by a unique string which depends on the content source. See [WithID] for more information.
/// In general, this is the flow:
/// 1) Generate a request from ImCache for a certain resource (could be a URL, a file, etc.)
/// 2) ImRequest implements [WithID], transforms the resource's src to a string
/// 3) Asks ImCache to store the resource once it's done
///
/// **Note:**
///
/// Despite the name and the fact that this is made to be used specifically for images, it still uses a generic `V` type
/// because different implementations may store data differently. For example, it may store images as a file
/// (it's the case of [DiskCache]) or directly as [ImImage] objects (it's the case of [MemoryCache]).
///
/// In the first case, the base methods defined by [Cache] will return [Files][File], which is likely not what the user
/// wants, 'cause, to use them, you would still have to convert them back.
/// (!! the conversion must be made by [ImageUtils#deserialize(File)] because `ImCache` uses a custom file format).
/// See [DiskCache#getImage(String)].
public abstract class ImgCache<V> implements Cache<V>, Iterable<Map.Entry<String, V>> {
    //================================================================================
    // Properties
    //================================================================================
    protected final SequencedMap<String, V> cache;
    protected int capacity = DEFAULT_CAPACITY;

    //================================================================================
    // Constructors
    //================================================================================
    public ImgCache() {
        this.cache = new LinkedHashMap<>();
    }

    protected ImgCache(SequencedMap<String, V> cache) {
        this.cache = cache;
    }

    //================================================================================
    // Abstract Methods
    //================================================================================

    /// Implementations should define here the logic to store images in the cache.
    public abstract void store(String id, ImImage img);

    /// Implementations should define here how to load and return an image from the cache.
    public abstract Optional<ImImage> getImage(String id);

    //================================================================================
    // Methods
    //================================================================================

    /// Delegates to [#store(String, ImImage)].
    public void store(WithID id, ImImage img) {
        store(id.id(), img);
    }

    /// Delegates to [#getImage(String)].
    public Optional<ImImage> getImage(WithID id) {
        return getImage(id.id());
    }

    /// Delegates to [SequencedMap#forEach(BiConsumer)].
    public void forEach(BiConsumer<String, V> consumer) {
        cache.forEach(consumer);
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public Iterator<Map.Entry<String, V>> iterator() {
        return asMap().entrySet().iterator();
    }

    @Override
    public Optional<V> get(String id) {
        return Optional.ofNullable(cache.get(id));
    }

    /// Stores the given cache entry in the backing data structure.
    ///
    /// - If the capacity is 0, exits immediately.
    /// - If capacity is reached, the oldest entry is removed first, see [#removeOldest()]
    @Override
    public void store(String id, V value) {
        if (capacity == 0) return;
        if (size() == capacity) removeOldest();
        cache.put(id, value);
    }

    /// Removes the cached resource associated with the given id.
    ///
    /// @return true if the resource was present and removed
    @Override
    public boolean remove(String id) {
        return Optional.ofNullable(cache.remove(id)).isPresent();
    }

    /// Remove the oldest cached entry.
    /// Since the default backing data structure is a [LinkedHashMap], the [SequencedMap#firstEntry()] is also the oldest.
    ///
    /// The logic here must be changed in case the type of data structure is changed.
    ///
    /// @return false if the cache is empty, otherwise the result of [#remove(String)]
    @Override
    public boolean removeOldest() {
        if (cache.isEmpty()) return false;
        return remove(cache.firstEntry().getKey());
    }

    /// @return whether the backing data structure contains a cached value for the given id
    @Override
    public boolean contains(String id) {
        return cache.containsKey(id);
    }

    /// Removes all entries from the backing data structure.
    @Override
    public void clear() {
        cache.clear();
    }

    /// @return the number of cached items in the backing data structure
    @Override
    public int size() {
        return cache.size();
    }

    /// @return an unmodifiable view of the backing data structure. The cache should be manipulated exclusively by the
    /// exposed API as implementations may define additional needed operations
    @Override
    public SequencedMap<String, V> asMap() {
        return Collections.unmodifiableSequencedMap(cache);
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public Cache<V> setCapacity(int capacity) {
        while (size() > capacity) removeOldest();
        this.capacity = capacity;
        return this;
    }
}
