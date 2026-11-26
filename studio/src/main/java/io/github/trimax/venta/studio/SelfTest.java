package io.github.trimax.venta.studio;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import io.github.trimax.venta.container.VentaApplication;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.studio.model.event.OutlineSelectionRequestEvent;
import io.github.trimax.venta.studio.model.outline.OutlineKind;
import io.github.trimax.venta.studio.services.SelectionService;
import io.github.trimax.venta.studio.utils.OutlineUtil;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lombok.SneakyThrows;

/**
 * Drives the window by itself and reports, for a check without a person at the screen: the camera is pushed with synthetic keys and
 * mouse drags, the window is resized as if its corner was dragged, and the heap is measured after a collection.
 */
final class SelfTest extends AnimationTimer {
    private final Stage stage;
    private final ViewportPanel viewport;
    private final FlyCamera flyCamera;
    private final int frames;

    private int frame;
    private float startX;
    private float startYaw;

    SelfTest(final Stage stage, final ViewportPanel viewport, final FlyCamera flyCamera, final int frames) {
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
            drag(40);
        }

        if (frame == 30 && System.getProperty("studio.select") != null)
            select(System.getProperty("studio.select"));

        if (frame == 25)
            viewport.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, "d", "d", KeyCode.D, false, false, false, false));

        if (frame >= 100 && frame < 220 && frame % 6 == 0) {
            stage.setWidth(1200 + (frame - 100) * 2);
            stage.setHeight(760 + (frame - 100));
        }

        if (frame % 600 == 0) {
            System.gc();
            System.out.printf("SELFTEST frame %d: heap %d MB, fps %.1f%n", frame,
                    (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) >> 20, viewport.getStatistics().getFramesPerSecond());
        }

        if (frame == frames)
            report();
    }

    /** Chooses an object of the scene in the hierarchy, as a click on it would */
    private void select(final String name) {
        final var outline = VentaApplication.getComponent(SelectionService.class).getOutline();

        OutlineUtil.items(outline, OutlineKind.Object).stream().filter(item -> item.name().equals(name)).findFirst()
                .ifPresent(item -> EventUtil.post(new OutlineSelectionRequestEvent(item)));
    }

    private void drag(final double deltaX) {
        Event.fireEvent(viewport, mouse(MouseEvent.MOUSE_PRESSED, 100, 100));
        Event.fireEvent(viewport, mouse(MouseEvent.MOUSE_DRAGGED, 100 + deltaX, 100));
        Event.fireEvent(viewport, mouse(MouseEvent.MOUSE_RELEASED, 100 + deltaX, 100));
    }

    private MouseEvent mouse(final EventType<MouseEvent> type, final double x, final double y) {
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
