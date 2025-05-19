package io.github.palexdev.imcache.cache;

import io.github.palexdev.imcache.core.ImImage;
import io.github.palexdev.imcache.exceptions.ImCacheException;
import io.github.palexdev.imcache.utils.ImageUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.SequencedMap;

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

    //================================================================================
    // Methods
    //================================================================================
    public DiskCache toDisk(Path savePath) {
        DiskCache dCache = new DiskCache(savePath);
        dCache.setCapacity(capacity);
        cache.forEach(dCache::store);
        return dCache;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public MemoryCache scan(Path scanPath) {
        if (scanPath == null || capacity == 0) return this;
        try {
            File[] files = scanPath.toFile().listFiles();
            if (files == null || files.length == 0) return this;
            Arrays.sort(files, Comparator.comparing(File::lastModified));
            for (File f : files) {
                if (size() == capacity) removeOldest();
                cache.put(WithID.generateId(f), ImageUtils.deserialize(f));
            }
        } catch (IOException ex) {
            throw new ImCacheException(
                "Failed to re-load cached images to memory during scan",
                ex
            );
        }
        return this;
    }

    @Override
    public Optional<ImImage> getImage(String id) {
        return get(id);
    }
}
