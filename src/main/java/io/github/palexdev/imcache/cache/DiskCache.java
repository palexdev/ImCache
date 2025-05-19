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

public class DiskCache extends ImgCache<File> {
    //================================================================================
    // Enum
    //================================================================================
    public enum ClearMode {
        NO_CLEAN {
            @Override
            void clear(DiskCache cache) {
                // No-op
            }
        },
        MEMORY {
            @Override
            void clear(DiskCache cache) {
                cache.clear();
            }
        },
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

    //================================================================================
    // Methods
    //================================================================================
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
    @Override
    public DiskCache scan(Path scanPath) {
        if (capacity == 0) return this;
        try (Stream<Path> stream = Files.list(savePath)) {
            stream.filter(f -> !Files.isDirectory(f))
                .map(Path::toFile)
                .sorted(Comparator.comparingLong(File::lastModified))
                .forEach(f -> {
                    if (size() == capacity) removeOldest();
                    cache.put(WithID.generateId(f), f);
                });
        } catch (IOException ex) {
            throw new ImCacheException(
                "Failed to re-load files during scan",
                ex
            );
        }
        return this;
    }

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

    @Override
    public Optional<ImImage> getImage(String id) {
        File file = null;
        Optional<File> opt = get(id);
        if (opt.isEmpty()) return Optional.empty();
        try {
            file = opt.get();
            return Optional.of(ImageUtils.deserialize(file));
        } catch (Exception ex) {
            throw new ImCacheException(
                "Failed to deserialize image from file %s because: %s"
                    .formatted(file.getName(), ex.getMessage()),
                ex
            );
        }
    }

    @Override
    public boolean remove(String id) {
        File file = cache.remove(id);
        if (file == null) return false;
        return delete(file);
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public Path getSavePath() {
        return savePath;
    }

    public DiskCache saveTo(Path savePath) {
        return saveTo(savePath, null);
    }

    public DiskCache saveTo(Path savePath, ClearMode clearMode) {
        Optional.ofNullable(clearMode).ifPresent(m -> m.clear(this));
        if (savePath == null) savePath = DEFAULT_CACHE_PATH;
        this.savePath = savePath;
        return this;
    }
}
