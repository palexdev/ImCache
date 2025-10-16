/*
 * Copyright (C) 2025 Parisi Alessandro - alessandro.parisi406@gmail.com
 * This file is part of ImCache (https://github.com/palexdev/imcache)
 *
 * ImCache is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * ImCache is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ImCache. If not, see <http://www.gnu.org/licenses/>.
 */

package tests;

import io.github.palexdev.imcache.cache.DiskCache;
import io.github.palexdev.imcache.cache.MemoryCache;
import io.github.palexdev.imcache.core.ImCache;
import io.github.palexdev.imcache.core.ImImage;
import io.github.palexdev.imcache.core.ImRequest;
import io.github.palexdev.imcache.core.ImRequest.RequestState;
import io.github.palexdev.imcache.transforms.*;
import io.github.palexdev.imcache.utils.TriConsumer;
import io.github.palexdev.imcache.utils.URLHandler;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class ImCacheTests {
    public static final String IMAGE_URL = "https://cdn.pixabay.com/photo/2024/04/09/03/04/ai-generated-8684869_960_720.jpg";
    public static final String GIF_URL = "https://media3.giphy.com/media/MT5UUV1d4CXE2A37Dg/200w.gif?cid=6c09b952atir21ebxac41fydue6xyxfrnena2lzmsr7a5n7p&ep=v1_gifs_search&rid=200w.gif&ct=g";
    private static Path TEMP_DIR;

    private static final TriConsumer<ImRequest.Result, FxRobot, ImageView> COMMON_CALLBACK = (r, rb, v) -> {
        r.src().ifPresent(i -> rb.interact(() -> Utils.setImage(v, i.asStream())));
        Utils.sleep(500);
        r.out().ifPresent(i -> rb.interact(() -> Utils.setImage(v, i.asStream())));
        Utils.sleep(500);
    };

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
        ImRequest request = ImCache.instance()
            .request((URL) null)
            .execute(r -> r.error().ifPresent(t -> System.err.println(t.getMessage())));
        assertSame(RequestState.FAILED, request.state());
    }

    @Test
    void testInvalidUrl() {
        ImRequest request = ImCache.instance()
            .request("This is an invalid url")
            .execute(r -> r.error().ifPresent(t -> System.err.println(t.getMessage())));
        assertSame(RequestState.FAILED, request.state());
    }

    @Test
    void testInvalidResource() {
        ImRequest request = ImCache.instance()
            .request("https://google.com")
            .execute(r -> r.error().ifPresent(t -> System.err.println(t.getMessage())));
        assertSame(RequestState.FAILED, request.state());
    }

    @Test
    void testDownloadAndOpen(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest.Result res = downloadImg().execute().result();
        assertSame(RequestState.SUCCEEDED, res.state());
        assertTrue(Files.exists(TEMP_DIR.resolve(res.id())));
        robot.interact(() -> Utils.setImage(view, res.unwrapOut().asStream()));
        Utils.sleep(500);
    }

    @Test
    void testGif(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest.Result res = downloadGif().execute().result();
        assertSame(RequestState.SUCCEEDED, res.state());
        assertTrue(Files.exists(TEMP_DIR.resolve(res.id())));
        robot.interact(() -> Utils.setImage(view, res.unwrapOut().asStream()));
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
        ImRequest request = downloadImg();
        request.executeAsync(null);
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
        ImCache.instance().cacheConfig(() -> DiskCache.load(TEMP_DIR).saveTo(TEMP_DIR));
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
        ((MemoryCache) ImCache.instance().storage()).toDisk(TEMP_DIR);
        ImCache.instance().storage().clear();
        assertEquals(0, ImCache.instance().storage().size());

        // Change cache object
        ImCache.instance().cacheConfig(() -> MemoryCache.load(TEMP_DIR));
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
            .request(saved)
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
            .execute(r -> COMMON_CALLBACK.accept(r, robot, view));
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testCircleCrop(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Resize(400, 400)) // Too big to show
            .transform(new CircleCrop(Color.WHITE, Color.RED, 5.0f))
            .execute(r -> COMMON_CALLBACK.accept(r, robot, view));
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testResize(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Resize(200, 200))
            .execute(r -> COMMON_CALLBACK.accept(r, robot, view));
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testResize2(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Resize(400, 400))
            .execute(r -> COMMON_CALLBACK.accept(r, robot, view));
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testRotate(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Rotate(45))
            .execute(r -> COMMON_CALLBACK.accept(r, robot, view));
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testFlipVertical(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Flip(Flip.FlipOrientation.VERTICAL))
            .execute(r -> COMMON_CALLBACK.accept(r, robot, view));
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testFlipHorizontal(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Flip(Flip.FlipOrientation.HORIZONTAL))
            .execute(r -> COMMON_CALLBACK.accept(r, robot, view));
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testFit(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Fit(360, 240))
            .execute(r -> COMMON_CALLBACK.accept(r, robot, view));
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testPad(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Pad(360, 240, Color.RED))
            .execute(r -> COMMON_CALLBACK.accept(r, robot, view));
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testGrayscale(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Grayscale())
            .execute(r -> COMMON_CALLBACK.accept(r, robot, view));
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testBrightness(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Brightness(100))
            .execute(r -> COMMON_CALLBACK.accept(r, robot, view));
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testBrightness2(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Brightness(-100))
            .execute(r -> COMMON_CALLBACK.accept(r, robot, view));
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testContrast(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Contrast(1.2f))
            .execute(r -> COMMON_CALLBACK.accept(r, robot, view));
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testContrast2(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Contrast(0.8f))
            .execute(r -> COMMON_CALLBACK.accept(r, robot, view));
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testAspectRatioCrop(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Resize(300, 300))
            .transform(new AspectRatioCrop(16, 9))
            .execute(r -> COMMON_CALLBACK.accept(r, robot, view));
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testGaussianBlur(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new GaussianBlur())
            .execute(r -> COMMON_CALLBACK.accept(r, robot, view));
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testVignette(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Resize(360, 360))
            .transform(new Vignette())
            .execute(r -> COMMON_CALLBACK.accept(r, robot, view));
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    @Test
    void testSketch(FxRobot robot) {
        ImageView view = Utils.setupStage();
        ImRequest request = downloadImg()
            .transform(new Sketch())
            .execute(r -> COMMON_CALLBACK.accept(r, robot, view));
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
            .execute(r -> COMMON_CALLBACK.accept(r, robot, view));
        assertSame(RequestState.SUCCEEDED, request.state());
    }

    //================================================================================
    // Common Methods
    //================================================================================
    private ImRequest downloadImg() {
        return ImCache.instance().request(IMAGE_URL);
    }

    private ImRequest downloadGif() {
        return ImCache.instance().request(GIF_URL);
    }
}
