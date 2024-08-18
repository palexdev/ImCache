package tests;

import io.github.palexdev.imcache.cache.DiskCache;
import io.github.palexdev.imcache.cache.MemoryCache;
import io.github.palexdev.imcache.core.ImCache;
import io.github.palexdev.imcache.core.ImImage;
import io.github.palexdev.imcache.core.ImRequest;
import io.github.palexdev.imcache.core.ImRequest.RequestState;
import io.github.palexdev.imcache.network.Downloader;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class ImCacheTests {
    public static final String IMAGE_URL = "https://cdn.pixabay.com/photo/2024/04/09/03/04/ai-generated-8684869_960_720.jpg";
    public static final String GIF_URL = "https://camo.githubusercontent.com/7de37139d0b4c1ce40865e799b446c0e963a3dd8fb68d239707237c40604fa3d/68747470733a2f2f63646e2e6472696262626c652e636f6d2f75736572732f3733303730332f73637265656e73686f74732f363538313234332f6176656e746f2e676966";
    private static Path TEMP_DIR;

    @BeforeEach
    void setup() throws IOException {
        // Prepare test directory
        TEMP_DIR = Files.createTempDirectory("imcachetests-");
        // Config cache
        ImCache.instance().cacheConfig(() -> new DiskCache().saveTo(TEMP_DIR));
        //ImCache.instance().cacheConfig(MemoryCache::new);
    }

    @AfterEach
    void cleanUp() throws IOException {
        File dir = TEMP_DIR.toFile();
        try {
            FileUtils.forceDelete(dir);
        } finally {
            FileUtils.forceDeleteOnExit(dir);
        }
    }

    @Start
    void start(Stage stage) {
        stage.show();
    }

    @Test
    void testDownload() {
        ImRequest request = downloadImg().execute();
        assertSame(RequestState.SUCCEEDED, request.state());
        assertTrue(Files.exists(TEMP_DIR.resolve(request.id())));
    }

    @Test
    void testNullUrl() {
        ImRequest request = ImCache.instance().request()
            .load(null)
            .onStateChanged(r -> r.error().ifPresent(t -> System.err.println(t.getMessage())))
            .execute();
        assertSame(RequestState.FAILED, request.state());
    }

    @Test
    void testInvalidUrl() {
        ImRequest request = ImCache.instance().request()
            .load("This is an invalid url")
            .onStateChanged(r -> r.error().ifPresent(t -> System.err.println(t.getMessage())))
            .execute();
        assertSame(RequestState.FAILED, request.state());
    }

    @Test
    void testInvalidResource() {
        ImRequest request = ImCache.instance().request()
            .load("https://google.com")
            .onStateChanged(r -> r.error().ifPresent(t -> System.err.println(t.getMessage())))
            .execute();
        assertSame(RequestState.FAILED, request.state());
    }

    @Test
    void testDownloadAndOpen(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .onStateChanged(r -> {
                if (r.state() == RequestState.SUCCEEDED) {
                    robot.interact(() -> Utils.setImage(view, r.unwrapOut().asStream()));
                }
            })
            .execute();
        assertSame(RequestState.SUCCEEDED, request.state());
        assertTrue(Files.exists(TEMP_DIR.resolve(request.id())));
        Utils.sleep(500);
    }

    @Test
    void testGif(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadGif()
            .onStateChanged(r -> {
                if (r.state() == RequestState.SUCCEEDED) {
                    robot.interact(() -> Utils.setImage(view, r.unwrapOut().asStream()));
                }
            })
            .execute();
        assertSame(RequestState.SUCCEEDED, request.state());
        assertTrue(Files.exists(TEMP_DIR.resolve(request.id())));
        Utils.sleep(1000);
    }

    @Test
    void testCacheHitMiss() {
        // Download both
        ImRequest imgRequest = downloadImg().execute();
        ImRequest gifRequest = downloadGif().execute();
        assertSame(RequestState.SUCCEEDED, imgRequest.state());
        assertSame(RequestState.SUCCEEDED, gifRequest.state());

        // Re-request and check state
        imgRequest.execute();
        gifRequest.execute();
        assertSame(RequestState.CACHE_HIT, imgRequest.state());
        assertSame(RequestState.CACHE_HIT, gifRequest.state());

        // Cause miss
        assertTrue(ImCache.instance().storage().remove(imgRequest));
        assertFalse(ImCache.instance().storage().contains(imgRequest));
        imgRequest.execute();
        assertSame(RequestState.SUCCEEDED, imgRequest.state()); // Re-download happened
    }

    @Test
    void testAsync() {
        ImRequest request = downloadImg().executeAsync();
        assertNotSame(RequestState.SUCCEEDED, request.state());
        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .until(() -> request.state() == RequestState.SUCCEEDED);
        assertSame(RequestState.SUCCEEDED, request.state());
        assertTrue(ImCache.instance().storage().contains(request));
    }

    @Test
    void testScanDisk(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg().execute();
        assertSame(RequestState.SUCCEEDED, request.state());
        assertEquals(1, ImCache.instance().storage().size());

        // Set a new cache object and scan
        ImCache.instance().cacheConfig(() -> new DiskCache().saveTo(TEMP_DIR));
        assertEquals(0, ImCache.instance().storage().size());
        ImCache.instance().storage().scan();
        assertEquals(1, ImCache.instance().storage().size());

        // Check image integrity
        ImCache.instance().storage()
            .getImage(request)
            .ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
        // Also check for ImImage objects integrity after deserialization
        for (Map.Entry<String, ?> stringEntry : ImCache.instance().storage()) {
            Optional<ImImage> img = ImCache.instance().storage().getImage(stringEntry.getKey());
            assertTrue(img.isPresent());
            assertNotNull(img.get().url());
            assertDoesNotThrow(() -> Downloader.toURL(img.get().url()));
        }
        Utils.sleep(500);
    }

    @Test
    void testScanMemory(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImCache.instance().cacheConfig(MemoryCache::new);
        ImRequest request = downloadImg().execute();
        assertSame(RequestState.SUCCEEDED, request.state());
        assertEquals(1, ImCache.instance().storage().size());

        // Save to disk
        ((MemoryCache) ImCache.instance().storage()).saveToDisk(TEMP_DIR);
        ImCache.instance().storage().clear();
        assertEquals(0, ImCache.instance().storage().size());

        // Change cache object
        ImCache.instance().cacheConfig(() -> new MemoryCache().scanPath(TEMP_DIR).scan());
        assertEquals(1, ImCache.instance().storage().size());

        // Check image integrity
        ImCache.instance().storage()
            .getImage(request)
            .ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
        // Also check for ImImage objects integrity after deserialization
        for (Map.Entry<String, ?> stringEntry : ImCache.instance().storage()) {
            Optional<ImImage> img = ImCache.instance().storage().getImage(stringEntry.getKey());
            assertTrue(img.isPresent());
            assertNotNull(img.get().url());
            assertDoesNotThrow(() -> Downloader.toURL(img.get().url()));
        }
        Utils.sleep(500);
    }

    //================================================================================
    // Common Methods
    //================================================================================
    private ImRequest downloadImg() {
        return ImCache.instance()
            .request()
            .load(IMAGE_URL);
    }

    private ImRequest downloadGif() {
        return ImCache.instance()
            .request()
            .load(GIF_URL);
    }
}
