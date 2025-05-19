package io.github.palexdev.imcache.cache;

import io.github.palexdev.imcache.core.ImImage;
import io.github.palexdev.imcache.exceptions.ImCacheException;
import io.github.palexdev.imcache.utils.ImageUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

public class MemoryCache extends Cache<ImImage> {
    private Path scanPath = Paths.get(System.getProperty("user.home"), "im-cache");

    //================================================================================
    // Methods
    //================================================================================
    public MemoryCache saveToDisk(Path savePath) {
        String id = null;
        try {
            for (Map.Entry<String, ImImage> e : cache.entrySet()) {
                id = e.getKey();
                ImImage img = e.getValue();
                Path path = savePath.resolve(id);
                ImageUtils.serialize(img, path.toFile());
            }
        } catch (IOException ex) {
            throw new ImCacheException(
                "Failed to save image %s from memory to disk"
                    .formatted(id),
                ex
            );
        }
        return this;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public MemoryCache scan() {
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
    public void store(String id, ImImage img) {
        if (capacity == 0) return;
        if (size() == capacity) removeOldest();
        cache.put(id, img);
    }

    @Override
    public boolean contains(String id) {
        return cache.containsKey(id);
    }

    @Override
    public Optional<ImImage> get(String id) {
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public Optional<ImImage> getImage(String id) {
        return get(id);
    }

    @Override
    public boolean remove(String id) {
        return Optional.ofNullable(cache.remove(id)).isPresent();
    }

    @Override
    public boolean removeOldest() {
        if (cache.isEmpty()) return false;
        return remove(cache.firstEntry().getKey());
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public Path getScanPath() {
        return scanPath;
    }

    public MemoryCache scanPath(Path scanPath) {
        this.scanPath = scanPath;
        return this;
    }
}
