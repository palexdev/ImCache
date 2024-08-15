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
            // TODO use logger?
        }
        return done;
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
                    if (size() == capacity) remove(cache.firstEntry().getKey());
                    cache.put(f.getName(), f);
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
    public void store(String name, Image img) {
        if (capacity == 0) return;
        if (size() == capacity) remove(cache.firstEntry().getKey());
        try {
            Path file = savePath.resolve(name);
            Files.createDirectories(file.getParent());
            Files.write(file, img.rawData(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            cache.put(name, file.toFile());
        } catch (Exception ex) {
            throw new ImCacheException(
                "Failed to store image %s in cache"
                    .formatted(name),
                ex
            );
        }
    }

    @Override
    public boolean contains(String name) {
        return cache.containsKey(name);
    }

    @Override
    public Optional<File> get(String name) {
        return Optional.ofNullable(cache.get(name));
    }

    @Override
    public Optional<Image> getImage(String name) {
        Optional<File> opt = get(name);
        if (opt.isEmpty()) return Optional.empty();
        File file = opt.get();
        // TODO for now the url is lost once loading the file
        //      A solution could be to use a second cache/database on the disk to store [name -> url]
        return Optional.of(Image.wrap(null, FileUtils.read(file)));
    }

    @Override
    public boolean remove(String name) {
        File file = cache.remove(name);
        if (file == null) return false;
        return delete(file);
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
