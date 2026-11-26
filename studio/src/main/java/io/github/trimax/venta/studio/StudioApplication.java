package io.github.trimax.venta.studio;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import io.github.trimax.venta.container.AbstractVentaApplication;
import io.github.trimax.venta.container.VentaApplication;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.core.Engine;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * The prototype of the editor: the engine draws the scene into the viewport of a JavaFX window, in the same process.
 *
 * <p>With {@code -Dstudio.selftest=N} it runs N frames, pushes the camera with synthetic keys and mouse drags, writes the picture
 * of the window to {@code -Dstudio.shot=file.png}, prints what it measured and exits.
 */
@Slf4j
@Component
public final class StudioApplication extends Application implements AbstractVentaApplication<Void> {
    private static String[] arguments = new String[0];

    private Engine engine;
    private ViewportPanel viewport;
    private AnimationTimer statusTimer;

    @Override
    public void start(final Stage stage) {
        engine = VentaApplication.getComponent(Engine.class);
        final var context = VentaApplication.getComponent(VentaContext.class);

        final var flyCamera = new FlyCamera();
        viewport = new ViewportPanel(engine, context, flyCamera);
        engine.startEmbedded(arguments, new StudioEngineApplication(flyCamera), viewport::onFrame);

        final var status = new Label("Starting…");
        status.setPadding(new Insets(3, 10, 3, 10));

        final var root = new BorderPane();
        root.setTop(createMenu(flyCamera));
        root.setLeft(createPanel("Hierarchy", "The tree of the scene will be here"));
        root.setCenter(viewport);
        root.setRight(createPanel("Inspector", "The properties of the selected object will be here"));
        root.setBottom(status);

        stage.setTitle("Venta Studio (prototype)");
        stage.setScene(new Scene(root, 1280, 800));
        stage.show();

        viewport.requestFocus();
        viewport.start();

        statusTimer = new AnimationTimer() {
            @Override
            public void handle(final long now) {
                final var statistics = viewport.getStatistics();
                status.setText(String.format("Viewport %s | %.0f fps | frame %.1f ms (reading the image %.1f ms) | Right mouse: look, W A S D Q E: fly, Shift: faster, wheel: dolly",
                        statistics.getSize(), statistics.getFramesPerSecond(), statistics.getFrameMilliseconds(), statistics.getReadMilliseconds()));
            }
        };
        statusTimer.start();

        final var selftest = Integer.getInteger("studio.selftest");
        if (selftest != null)
            new SelfTest(stage, viewport, flyCamera, selftest).start();
    }

    @Override
    public void start(final String[] args, final Void argument) {
        arguments = args;
        launch(StudioApplication.class, args);
    }

    @Override
    public void stop() {
        if (statusTimer != null)
            statusTimer.stop();

        if (viewport != null)
            viewport.stop();

        if (engine != null)
            engine.shutdown();
    }

    private MenuBar createMenu(final FlyCamera flyCamera) {
        final var quit = new MenuItem("Quit");
        quit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN));
        quit.setOnAction(event -> Platform.exit());

        final var reset = new MenuItem("Reset camera");
        reset.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN));
        reset.setOnAction(_ -> flyCamera.reset());

        final var bar = new MenuBar(new Menu("File", null, quit), new Menu("View", null, reset));
        bar.setUseSystemMenuBar(true);
        return bar;
    }

    private VBox createPanel(final String title, final String text) {
        final var heading = new Label(title);
        heading.setStyle("-fx-font-weight: bold;");

        final var panel = new VBox(8, heading, new Label(text));
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(220);
        panel.setMinWidth(220);
        return panel;
    }

    static void main(final String[] args) {
        VentaApplication.runScanning(args, StudioApplication.class, "io.github.trimax.venta.engine");
    }

    /** Drives the window by itself and reports, for a check without a person at the screen */
    private static final class SelfTest extends AnimationTimer {
        private final Stage stage;
        private final ViewportPanel viewport;
        private final FlyCamera flyCamera;
        private final int frames;

        private int frame;
        private float startX;
        private float startYaw;

        private SelfTest(final Stage stage, final ViewportPanel viewport, final FlyCamera flyCamera, final int frames) {
            this.stage = stage;
            this.viewport = viewport;
            this.flyCamera = flyCamera;
            this.frames = frames;
        }

        @Override
        public void handle(final long now) {
            frame++;

            if (frame == 5) {
                startX = flyCamera.getPosition().x;
                startYaw = flyCamera.getYaw();
                viewport.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "d", "d", KeyCode.D, false, false, false, false));
                fireDrag(40);
            }

            if (frame == 25)
                viewport.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, "d", "d", KeyCode.D, false, false, false, false));

            // The window is resized a few times, as a person would drag its corner
            if (frame >= 100 && frame < 220 && frame % 6 == 0) {
                stage.setWidth(1000 + (frame - 100) * 2);
                stage.setHeight(700 + (frame - 100));
            }

            if (frame == 230)
                System.out.printf("SELFTEST after the resize: viewport=%s%n", viewport.getStatistics().getSize());

            if (frame % 600 == 0) {
                System.gc();
                System.out.printf("SELFTEST frame %d: heap %d MB, fps %.1f%n", frame,
                        (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) >> 20, viewport.getStatistics().getFramesPerSecond());
            }

            if (frame == frames)
                report();
        }

        private void fireDrag(final double deltaX) {
            Event.fireEvent(viewport, mouse(MouseEvent.MOUSE_PRESSED, 100, 100));
            Event.fireEvent(viewport, mouse(MouseEvent.MOUSE_DRAGGED, 100 + deltaX, 100));
            Event.fireEvent(viewport, mouse(MouseEvent.MOUSE_RELEASED, 100 + deltaX, 100));
        }

        private MouseEvent mouse(final javafx.event.EventType<MouseEvent> type, final double x, final double y) {
            return new MouseEvent(type, x, y, x, y, MouseButton.SECONDARY, 1, false, false, false, false, false, false, true, false, false, false, null);
        }

        @SneakyThrows
        private void report() {
            stop();

            final var statistics = viewport.getStatistics();
            System.out.printf("SELFTEST viewport=%s fps=%.1f frame=%.2fms read=%.2fms%n", statistics.getSize(), statistics.getFramesPerSecond(),
                    statistics.getFrameMilliseconds(), statistics.getReadMilliseconds());
            System.out.printf("SELFTEST camera moved by the key: %.2f m, turned by the mouse: %.3f rad%n",
                    flyCamera.getPosition().x - startX, flyCamera.getYaw() - startYaw);

            final var shot = System.getProperty("studio.shot");
            if (shot != null) {
                final var image = viewport.getScene().getRoot().snapshot(null, null);
                final var width = (int) image.getWidth();
                final var height = (int) image.getHeight();
                final var output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                for (int y = 0; y < height; y++)
                    for (int x = 0; x < width; x++)
                        output.setRGB(x, y, image.getPixelReader().getArgb(x, y));

                ImageIO.write(output, "png", new File(shot));
                System.out.println("SELFTEST picture " + shot);
            }

            Platform.exit();
        }
    }
}
