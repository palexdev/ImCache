package tests;

import io.github.palexdev.imcache.cache.DiskCache;
import io.github.palexdev.imcache.cache.MemoryCache;
import io.github.palexdev.imcache.core.ImCache;
import io.github.palexdev.imcache.core.ImImage;
import io.github.palexdev.imcache.core.ImRequest;
import io.github.palexdev.imcache.core.ImRequest.RequestState;
import io.github.palexdev.imcache.transforms.*;
import io.github.palexdev.imcache.utils.URLHandler;
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

import java.awt.*;
import java.awt.desktop.OpenURIHandler;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.server.UID;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class ImCacheTests {
    public static final String IMAGE_URL = "https://cdn.pixabay.com/photo/2024/04/09/03/04/ai-generated-8684869_960_720.jpg";
    public static final String GIF_URL = "https://media3.giphy.com/media/MT5UUV1d4CXE2A37Dg/200w.gif?cid=6c09b952atir21ebxac41fydue6xyxfrnena2lzmsr7a5n7p&ep=v1_gifs_search&rid=200w.gif&ct=g";
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
            .load((URL) null)
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
        }
        Utils.sleep(500);
    }

    @Test
    void testLocalRequest(FxRobot robot) throws IOException {
        ImageView view = Utils.setupStage();

        // Download image to local
        byte[] raw = URLHandler.resolve(URLHandler.toURL(IMAGE_URL).orElse(null), null);
        Path saved = Files.write(TEMP_DIR.resolve("image.jpg"), raw);
        robot.interact(() -> Utils.setImage(view, saved));
        Utils.sleep(500);

        // Reset view
        robot.interact(() -> view.setImage(null));

        // Local request
        ImRequest req = ImCache.instance()
            .request()
            .load(saved.toUri().toURL())
            .execute();
        assertSame(RequestState.SUCCEEDED, req.state());
        assertTrue(ImCache.instance().storage().contains(req));

        // Check image integrity
        ImCache.instance().storage()
            .getImage(req)
            .ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
        // Also check for ImImage objects integrity after deserialization
        for (Map.Entry<String, ?> stringEntry : ImCache.instance().storage()) {
            Optional<ImImage> img = ImCache.instance().storage().getImage(stringEntry.getKey());
            assertTrue(img.isPresent());
            assertNotNull(img.get().url());
        }
        Utils.sleep(500);
    }

    @Test
    void testCenterCrop(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new CenterCrop(200, 200))
            .onStateChanged(r -> {
                r.src().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
                r.out().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
            })
            .execute();
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testCircleCrop(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Resize(400, 400)) // Too big to show
            .transform(new CircleCrop(Color.WHITE, Color.RED, 5.0f))
            .onStateChanged(r -> {
                r.src().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
                r.out().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(1000);
            })
            .execute();
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testResize(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Resize(200, 200))
            .onStateChanged(r -> {
                r.src().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
                r.out().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
            })
            .execute();
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testResize2(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Resize(400, 400))
            .onStateChanged(r -> {
                r.src().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
                r.out().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
            })
            .execute();
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testRotate(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Rotate(45))
            .onStateChanged(r -> {
                r.src().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
                r.out().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
            })
            .execute();
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testFlipVertical(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Flip(Flip.FlipOrientation.VERTICAL))
            .onStateChanged(r -> {
                r.src().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
                r.out().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
            })
            .execute();
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testFlipHorizontal(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Flip(Flip.FlipOrientation.HORIZONTAL))
            .onStateChanged(r -> {
                r.src().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
                r.out().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
            })
            .execute();
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testFit(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Fit(360, 240))
            .onStateChanged(r -> {
                r.src().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
                r.out().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
            })
            .execute();
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testPad(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Pad(360, 240, Color.RED))
            .onStateChanged(r -> {
                r.src().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
                r.out().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
            })
            .execute();
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testGrayscale(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Grayscale())
            .onStateChanged(r -> {
                r.src().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
                r.out().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
            })
            .execute();
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testBrightness(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Brightness(100))
            .onStateChanged(r -> {
                r.src().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
                r.out().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
            })
            .execute();
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testBrightness2(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Brightness(-100))
            .onStateChanged(r -> {
                r.src().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
                r.out().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
            })
            .execute();
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testContrast(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Contrast(1.2f))
            .onStateChanged(r -> {
                r.src().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
                r.out().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
            })
            .execute();
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testContrast2(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Contrast(0.8f))
            .onStateChanged(r -> {
                r.src().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
                r.out().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
            })
            .execute();
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testAspectRatioCrop(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Resize(300, 300))
            .transform(new AspectRatioCrop(16, 9))
            .onStateChanged(r -> {
                r.src().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
                r.out().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
            })
            .execute();
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testGaussianBlur(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new GaussianBlur())
            .onStateChanged(r -> {
                r.src().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
                r.out().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
            })
            .execute();
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testVignette(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Resize(360, 360))
            .transform(new Vignette())
            .onStateChanged(r -> {
                r.src().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
                r.out().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
            })
            .execute();
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testSketch(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Sketch())
            .onStateChanged(r -> {
                r.src().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
                r.out().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
            })
            .execute();
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testAddText(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new AddText("Hello\nWorld!", AddText.Position.CENTER)
                .setFont(new Font(null, Font.BOLD, 36))
                .setYOffset(-30)
                .setXOffset(100)
            )
            .onStateChanged(r -> {
                r.src().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
                r.out().ifPresent(i -> robot.interact(() -> Utils.setImage(view, i.asStream())));
                Utils.sleep(500);
            })
            .execute();
        assertSame(RequestState.SUCCEEDED, request.state());
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
