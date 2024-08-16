package io.github.palexdev.imcache.cache;

import io.github.palexdev.imcache.core.Image;
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

public class MemoryCache extends Cache<Image> {
    private Path scanPath = Paths.get(System.getProperty("user.home"), "im-cache");

    //================================================================================
    // Methods
    //================================================================================
    public MemoryCache saveToDisk(Path savePath) {
        String id = null;
        try {
            for (Map.Entry<String, Image> e : cache.entrySet()) {
                id = e.getKey();
                Image img = e.getValue();
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
                    if (size() == capacity) remove(cache.firstEntry().getKey()); // TODO add method to remove oldest entry
                    cache.put(f.getName(), Image.wrap(null, FileUtils.read(f)));
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
    public void store(String id, Image img) {
        if (capacity == 0) return;
        if (size() == capacity) cache.pollFirstEntry();
        cache.put(id, img);
    }

    @Override
    public boolean contains(String id) {
        return cache.containsKey(id);
    }

    @Override
    public Optional<Image> get(String id) {
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public Optional<Image> getImage(String id) {
        return get(id);
    }

    @Override
    public boolean remove(String id) {
        return Optional.ofNullable(cache.remove(id)).isPresent();
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
