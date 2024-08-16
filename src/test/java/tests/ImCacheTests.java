package tests;

import io.github.palexdev.imcache.cache.DiskCache;
import io.github.palexdev.imcache.cache.MemoryCache;
import io.github.palexdev.imcache.core.ImCache;
import io.github.palexdev.imcache.core.Request;
import io.github.palexdev.imcache.core.Request.RequestState;
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
        Request request = downloadImg().execute();
        assertSame(RequestState.SUCCEEDED, request.state());
        assertTrue(Files.exists(TEMP_DIR.resolve(ImCache.instance().storage().toName(request))));
    }

    @Test
    void testDownloadAndOpen(FxRobot robot) {
        ImageView view = Utils.setupStage();
        Request request = downloadImg()
                .onSuccess((r, src, out) -> robot.interact(() -> Utils.setImage(view, out.asStream())))
                .execute();
        assertSame(RequestState.SUCCEEDED, request.state());
        assertTrue(Files.exists(TEMP_DIR.resolve(ImCache.instance().storage().toName(request))));
        Utils.sleep(1000);
    }

    @Test
    void testGif(FxRobot robot) {
        ImageView view = Utils.setupStage();
        Request request = downloadGif()
                .onSuccess((r, src, out) -> robot.interact(() -> Utils.setImage(view, out.asStream())))
                .execute();
        assertSame(RequestState.SUCCEEDED, request.state());
        assertTrue(Files.exists(TEMP_DIR.resolve(ImCache.instance().storage().toName(request))));
        Utils.sleep(2000);
    }

    @Test
    void testCacheHitMiss() {
        // Download both
        Request imgRequest = downloadImg().execute();
        Request gifRequest = downloadGif().execute();
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
        Request request = downloadImg().executeAsync();
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
        Request request = downloadImg().execute();
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
    }

    @Test
    void testScanMemory(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImCache.instance().cacheConfig(MemoryCache::new);
        Request request = downloadImg().execute();
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
    }

    //================================================================================
    // Common Methods
    //================================================================================
    private Request downloadImg() {
        return ImCache.instance()
                .request()
                .load(IMAGE_URL);
    }

    private Request downloadGif() {
        return ImCache.instance()
                .request()
                .load(GIF_URL);
    }
}
