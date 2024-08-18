package io.github.palexdev.imcache.cache;

import io.github.palexdev.imcache.core.ImImage;
import io.github.palexdev.imcache.exceptions.ImCacheException;
import io.github.palexdev.imcache.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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
                Files.write(path, img.rawData(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
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
        try (Stream<Path> stream = Files.list(scanPath)) {
            stream.filter(f -> !Files.isDirectory(f))
                .map(Path::toFile)
                .sorted(Comparator.comparingLong(File::lastModified))
                .forEach(f -> {
                    if (size() == capacity) removeOldest();
                    cache.put(f.getName(), ImImage.wrap(null, FileUtils.read(f)));
                });
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
