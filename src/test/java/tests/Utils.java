package tests;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.testfx.api.FxToolkit;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;

public class Utils {

    //================================================================================
    // Constructors
    //================================================================================
    private Utils() {}

    //================================================================================
    // Static Methods
    //================================================================================
    public static ImageView setupStage() {
        ImageView view = new ImageView();
        StackPane pane = new StackPane(view);
        try {
            Scene scene = new Scene(pane, 400, 400);
            FxToolkit.setupStage(s -> {
                s.setWidth(400);
                s.setHeight(400);
                s.setScene(scene);
            });
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        return view;
    }

    public static void setImage(ImageView view, Path file) {
        try (FileInputStream fis = new FileInputStream(file.toFile())) {
            setImage(view, fis);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void setImage(ImageView view, BufferedImage image) {
        view.setImage(SwingFXUtils.toFXImage(image, null));
    }

    public static void setImage(ImageView view, InputStream is) {
        view.setImage(new Image(is));
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ignored) {}
    }
}
