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
import io.github.palexdev.imcache.exceptions.ImCacheException;
import io.github.palexdev.imcache.utils.ImageUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.stream.Stream;

/// Simplest concrete implementation of [ImgCache]. Images are directly stored in memory as they are, and therefore
/// [#getImage(String)] is a direct call to the backing data structure.
///
/// That said, this cache still offers some operations that involve persistent storage.
/// 1) It's possible to convert this to a [DiskCache] via [#toDisk(Path)]
/// 2) It's possible to load previously persisted images via [#load(Path)] or [#load(Path, int)]
public class MemoryCache extends ImgCache<ImImage> {

    //================================================================================
    // Constructors
    //================================================================================
    public MemoryCache() {
        super();
    }

    protected MemoryCache(SequencedMap<String, ImImage> cache) {
        super(cache);
    }

    /// Delegates to [#load(Path, int)] with capacity set to [#DEFAULT_CAPACITY]
    public static MemoryCache load(Path loadPath) {
        return load(loadPath, DEFAULT_CAPACITY);
    }

    /// Creates a new [MemoryCache] object and loads previously persisted images from the given path.
    ///
    /// Files are deserialized to images with [ImageUtils#deserialize(File)] and stored in the cache with an id generated
    /// from the image's url, [WithID#generateId(URL)].
    public static MemoryCache load(Path loadPath, int capacity) {
        MemoryCache cache = new MemoryCache();
        cache.capacity = capacity;
        try (Stream<Path> stream = Files.list(loadPath)) {
            stream.filter(f -> !Files.isDirectory(f))
                .map(Path::toFile)
                .sorted(Comparator.comparingLong(File::lastModified))
                .forEach(f -> {
                    try {
                        ImImage img = ImageUtils.deserialize(f);
                        if (cache.size() == capacity) cache.removeOldest();
                        cache.cache.put(WithID.generateId(img.url()), img);
                    } catch (IOException ex) {
                        throw new ImCacheException(
                            "Failed to reload cached image from file " + f,
                            ex
                        );
                    }
                });
        } catch (IOException ex) {
            throw new ImCacheException(
                "An error occurred while reloading images from path " + loadPath,
                ex
            );
        }
        return cache;
    }

    //================================================================================
    // Methods
    //================================================================================

    /// Creates a new [DiskCache] object, sets its capacity the same as this cache and finally calls [DiskCache#store(String, ImImage)]
    /// on each entry in this cache.
    public DiskCache toDisk(Path savePath) {
        DiskCache dCache = new DiskCache(savePath);
        dCache.setCapacity(capacity);
        forEach(dCache::store);
        return dCache;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================

    /// Direct access to the backing data structure, [#get(String)].
    @Override
    public Optional<ImImage> getImage(String id) {
        return get(id);
    }
}
