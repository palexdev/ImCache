package io.github.palexdev.imcache.cache;

import io.github.palexdev.imcache.core.ImImage;
import io.github.palexdev.imcache.exceptions.ImCacheException;
import io.github.palexdev.imcache.utils.ImageUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public class DiskCache extends Cache<File> {
    //================================================================================
    // Properties
    //================================================================================
    private Path savePath = Paths.get(System.getProperty("user.home"), "im-cache");

    //================================================================================
    // Methods
    //================================================================================
    protected boolean delete(File file) {
        boolean done = file.delete();
        if (!done) {
            throw new ImCacheException(
                "Failed to delete file %s"
                    .formatted(file)
            );
        }
        return true;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public DiskCache scan() {
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
        if (capacity == 0) return;
        if (size() == capacity) removeOldest();
        try {
            Path path = savePath.resolve(id);
            File file = path.toFile();
            Files.createDirectories(path.getParent());
            ImageUtils.serialize(img, file);
            cache.put(id, file);
        } catch (Exception ex) {
            throw new ImCacheException(
                "Failed to store image %s in cache"
                    .formatted(id),
                ex
            );
        }
    }

    @Override
    public boolean contains(String id) {
        return cache.containsKey(id);
    }

    @Override
    public Optional<File> get(String id) {
        return Optional.ofNullable(cache.get(id));
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

    @Override
    public boolean removeOldest() {
        if (cache.isEmpty()) return false;
        return remove(cache.firstEntry().getKey());
    }

    @Override
    public void clear() {
        for (File f : cache.values()) delete(f);
        super.clear();
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public Path getSavePath() {
        return savePath;
    }

    public DiskCache saveTo(Path savePath) {
        return saveTo(savePath, false);
    }

    public DiskCache saveTo(Path savePath, boolean cleanUp) {
        if (cleanUp) cache.values().forEach(this::delete);
        cache.clear();

        if (savePath == null) savePath = Paths.get(System.getProperty("user.home"), "im-cache");
        this.savePath = savePath;
        return this;
    }
}
