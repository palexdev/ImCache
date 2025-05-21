package io.github.palexdev.imcache.cache;

import io.github.palexdev.imcache.core.ImImage;
import io.github.palexdev.imcache.exceptions.ImCacheException;
import io.github.palexdev.imcache.utils.ImageUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.stream.Stream;

/// Concrete implementation of [DiskCache] that stores images on the file system and keeps them in memory as [Files][File].
/// By default, the save path is set to [#DEFAULT_CACHE_PATH].
///
/// Images are serialized to files with [ImageUtils#serialize(ImImage, File)] and deserialized with
/// [ImageUtils#deserialize(File)].
///
/// Methods such as [#store(String, ImImage)] and [#getImage(String)] automatically perform the aforementioned operations
/// for you, allowing to work directly with images.
///
/// _Note:_
///
/// When changing the save path, you are also asked to decide what to do with previously cached entries. The behavior is
/// defined by the [ClearMode] enumeration.
public class DiskCache extends ImgCache<File> {
    //================================================================================
    // Enum
    //================================================================================
    public enum ClearMode {

        /// No-op. Doesn't clear anything.
        NO_CLEAN {
            @Override
            void clear(DiskCache cache) {
                // No-op
            }
        },

        /// Removes cached entries only from the memory.
        MEMORY {
            @Override
            void clear(DiskCache cache) {
                cache.clear();
            }
        },

        // Removes and deletes entries from the memory and the disk.
        DISK_AND_MEMORY {
            @Override
            void clear(DiskCache cache) {
                cache.cache.values().forEach(cache::delete);
                cache.clear();
            }
        };

        abstract void clear(DiskCache cache);
    }

    //================================================================================
    // Properties
    //================================================================================
    private Path savePath = DEFAULT_CACHE_PATH;

    //================================================================================
    // Constructors
    //================================================================================
    public DiskCache() {
        super();
    }

    public DiskCache(Path savePath) {
        this.savePath = savePath;
    }

    protected DiskCache(SequencedMap<String, File> cache, Path savePath) {
        super(cache);
        this.savePath = savePath;
    }

    /// Delegates to [#load(Path, int)] with capacity set to [#DEFAULT_CAPACITY].
    public static DiskCache load(Path loadPath) {
        return load(loadPath, DEFAULT_CAPACITY);
    }

    /// Creates a new [DiskCache] object and loads previously persisted images from the given path.
    ///
    /// Differently from [MemoryCache#load(Path, int)], files are not deserialized because this cache stores images
    /// indirectly as [Files][File]. They are deserialized only when requested by [#getImage(String)].
    public static DiskCache load(Path loadPath, int capacity) {
        DiskCache cache = new DiskCache();
        cache.capacity = capacity;
        try (Stream<Path> stream = Files.list(loadPath)) {
            stream.filter(f -> !Files.isDirectory(f))
                .map(Path::toFile)
                .sorted(Comparator.comparingLong(File::lastModified))
                .forEach(f -> {
                    if (cache.size() == capacity) cache.removeOldest();
                    cache.cache.put(WithID.generateId(f), f);
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

    /// Attempts to delete the given file. Typically called when removing cached entries.
    protected boolean delete(File file) {
        if (!file.exists()) return true;

        boolean done = file.delete();
        if (!done) {
            throw new ImCacheException(
                "Failed to delete file %s"
                    .formatted(file)
            );
        }
        return true;
    }

    public void clear(ClearMode mode) {
        mode.clear(this);
    }

    //================================================================================
    // Overridden Methods
    //================================================================================

    /// Creates a file in the cache directory ([#getSavePath()]) with the given id as the name. Serialized the given image
    /// to the file with [ImageUtils#serialize(ImImage, File)] and finally delegates to [#store(String, Object)] to cache
    /// the entry.
    @Override
    public void store(String id, ImImage img) {
        try {
            Path path = savePath.resolve(id);
            File file = path.toFile();
            Files.createDirectories(path.getParent());
            ImageUtils.serialize(img, file);
            store(id, file);
        } catch (Exception ex) {
            throw new ImCacheException(
                "Failed to store image %s in cache"
                    .formatted(id),
                ex
            );
        }
    }

    /// Retrieves the cached file for the given id and then deserializes it with [ImageUtils#deserialize(File)],
    /// returning an [Optional] to indicate whether the cache entry was present and successfully deserialized or not.
    @Override
    public Optional<ImImage> getImage(String id) {
        return get(id).flatMap(f -> {
            try {
                return Optional.of(ImageUtils.deserialize(f));
            } catch (IOException ex) {
                throw new ImCacheException(
                    "Failed to deserialize image from file %s because: %s"
                        .formatted(f.getName(), ex.getMessage()),
                    ex
                );
            }
        });
    }

    /// Attempts to remove a cached entry for the given id, and if it is found, it's also deleted from the disk with [#delete(File)].
    ///
    /// @return true if the entry was present and deleted, otherwise false.
    @Override
    public boolean remove(String id) {
        return Optional.ofNullable(cache.remove(id))
            .map(this::delete)
            .orElse(false);
    }

    //================================================================================
    // Getters/Setters
    //================================================================================

    /// @return the path where cached resources are stored
    public Path getSavePath() {
        return savePath;
    }

    /// Delegates to [#saveTo(Path, ClearMode)] with clear mode set to [ClearMode#NO_CLEAN].
    public DiskCache saveTo(Path savePath) {
        return saveTo(savePath, ClearMode.NO_CLEAN);
    }

    /// Changes the path where cached resources are stored and clears the previous cache directory with the given clear mode.
    ///
    /// @see ClearMode
    public DiskCache saveTo(Path savePath, ClearMode clearMode) {
        clearMode.clear(this);
        if (savePath == null) savePath = DEFAULT_CACHE_PATH;
        this.savePath = savePath;
        return this;
    }
}
