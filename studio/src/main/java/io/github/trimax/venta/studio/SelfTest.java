package io.github.trimax.venta.studio;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import io.github.trimax.venta.container.VentaApplication;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.studio.model.event.OutlineSelectionRequestEvent;
import io.github.trimax.venta.studio.model.outline.OutlineKind;
import io.github.trimax.venta.studio.services.PlacementService;
import io.github.trimax.venta.studio.services.SceneApplier;
import io.github.trimax.venta.studio.services.SceneEditService;
import io.github.trimax.venta.studio.services.SceneService;
import io.github.trimax.venta.studio.services.SelectionService;
import io.github.trimax.venta.studio.utils.EntryUtil;
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

        if (frame == 5 && System.getProperty("studio.camera") == null) {
            startX = flyCamera.getPosition().x;
            startYaw = flyCamera.getYaw();
            viewport.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "d", "d", KeyCode.D, false, false, false, false));
            drag(40);
        }

        if (frame == 30 && System.getProperty("studio.select") != null)
            select(System.getProperty("studio.select"));

        if (frame == 20 && System.getProperty("studio.wireframe") != null)
            VentaApplication.getComponent(SceneService.class).setWireframeEverywhere(true);

        if (frame == 40 && System.getProperty("studio.menu") != null) {
            final var button = (javafx.scene.control.MenuButton) stage.getScene().lookup("#wireframe");
            System.out.println("SELFTEST menu button found: " + (button != null));
            if (button != null)
                button.show();
        }

        if (frame == 60 && System.getProperty("studio.menu") != null) {
            final var button = (javafx.scene.control.MenuButton) stage.getScene().lookup("#wireframe");
            try {
                final var field = javafx.scene.control.skin.MenuButtonSkinBase.class.getDeclaredField("popup");
                field.setAccessible(true);
                final var popup = (javafx.scene.control.ContextMenu) field.get(button.getSkin());
                System.out.println("SELFTEST popup: showing=" + popup.isShowing() + ", items=" + popup.getItems().size() + "/" + button.getItems().size() + ", skin=" + button.getSkin().getClass().getSimpleName()
                        + ", disabled=" + button.isDisabled() + ", visible=" + button.isVisible() + ", scene=" + (button.getScene() != null) + ", focused=" + button.isFocused());
            } catch (final ReflectiveOperationException exception) {
                System.out.println("SELFTEST popup: " + exception);
            }
            System.out.println("SELFTEST menu showing: " + button.isShowing() + ", windows: " + javafx.stage.Window.getWindows().stream()
                    .map(window -> window.getClass().getSimpleName() + (window.isShowing() ? "(shown)" : "(hidden)")).toList());
        }

        if (frame == 3 && System.getProperty("studio.camera") != null) {
            final var values = System.getProperty("studio.camera").split(",");
            flyCamera.place(Float.parseFloat(values[0]), Float.parseFloat(values[1]), Float.parseFloat(values[2]), Float.parseFloat(values[3]), Float.parseFloat(values[4]));
        }

        if (System.getProperty("studio.edit") != null)
            edit();

        if (System.getProperty("studio.create") != null)
            structure();

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

        final var shots = System.getProperty("studio.shot.frames");
        if (shots != null && System.getProperty("studio.shot") != null)
            for (final var number : shots.split(","))
                if (frame == Integer.parseInt(number.trim()))
                    writePicture(System.getProperty("studio.shot").replace(".png", "-" + frame + ".png"));

        if (frame == frames)
            report();
    }

    /** Moves the chosen object, undoes the move, does it again, saves the scene and reads the file: the whole of the editing, with the numbers */
    private void edit() {
        final var edits = VentaApplication.getComponent(SceneEditService.class);
        final var applier = VentaApplication.getComponent(SceneApplier.class);
        final var name = System.getProperty("studio.edit");
        final var outline = VentaApplication.getComponent(SelectionService.class).getOutline();
        final var item = OutlineUtil.items(outline, OutlineKind.Object).stream().filter(candidate -> candidate.name().equals(name)).findFirst().orElse(null);
        if (item == null)
            return;

        final var engine = (io.github.trimax.venta.engine.model.instance.ObjectInstance) applier.instanceOf(OutlineKind.Object, item.index());

        if (frame == 50) {
            System.out.printf("SELFTEST edit: before the move x=%.2f%n", engine.getPosition().x());
            edits.edit(OutlineKind.Object, item.index(), "Move", "position", entry -> {
                final var position = EntryUtil.getVector(entry, EntryUtil.POSITION, new org.joml.Vector3f());
                EntryUtil.setVector(entry, EntryUtil.POSITION, position.add(1.f, 0.f, 0.f));
            });
            System.out.printf("SELFTEST edit: moved x=%.2f, file x=%.2f, dirty=%s%n", engine.getPosition().x(),
                    EntryUtil.getVector(edits.getDocument().getEntry(OutlineKind.Object, item.index()), EntryUtil.POSITION, new org.joml.Vector3f()).x, edits.isDirty());
        }

        if (frame == 60) {
            edits.undo();
            System.out.printf("SELFTEST edit: undone x=%.2f, dirty=%s%n", engine.getPosition().x(), edits.isDirty());
        }

        if (frame == 70) {
            edits.redo();
            final var saved = edits.save();
            System.out.printf("SELFTEST edit: redone x=%.2f, saved=%s, dirty=%s%n", engine.getPosition().x(), saved, edits.isDirty());
        }
    }

    /** Puts a file of the project in the scene, copies it, deletes it and takes everything back: the counts of the things of the file and of the engine */
    private void structure() {
        final var edits = VentaApplication.getComponent(SceneEditService.class);
        final var context = VentaApplication.getComponent(io.github.trimax.venta.engine.context.VentaContext.class);
        final var session = VentaApplication.getComponent(StudioSession.class);
        final var placement = VentaApplication.getComponent(PlacementService.class);
        final var step = switch (frame) {
            case 50 -> "created";
            case 60 -> "undone";
            case 70 -> "redone";
            case 80 -> "duplicated";
            case 90 -> "deleted";
            case 100 -> "undone the delete";
            case 110 -> "moved the copy";
            default -> null;
        };
        if (step == null)
            return;

        switch (frame) {
            case 50 -> placement.place(session.getProject().resolve(System.getProperty("studio.create")), 0.5f, 0.8f);
            case 60, 100 -> edits.undo();
            case 70 -> edits.redo();
            case 80 -> edits.duplicate(OutlineKind.Object, edits.getDocument().count(OutlineKind.Object) - 1);
            case 90 -> edits.delete(OutlineKind.Object, edits.getDocument().count(OutlineKind.Object) - 1);
            default -> {
            }
        }

        final var count = edits.getDocument().count(OutlineKind.Object);
        final var last = edits.getDocument().getEntry(OutlineKind.Object, count - 1);
        System.out.printf("SELFTEST structure: %s: file has %d objects, engine has %d, last is `%s` at %s, dirty=%s%n", step, count,
                context.getSceneManager().getCurrent().getObjects().size(), EntryUtil.getName(last), EntryUtil.getVector(last, EntryUtil.POSITION, new org.joml.Vector3f()),
                edits.isDirty());
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

    /** The picture of the whole window */
    @SneakyThrows
    private void writePicture(final String file) {
        final var image = viewport.getScene().getRoot().snapshot(null, null);
        final var width = (int) image.getWidth();
        final var height = (int) image.getHeight();
        final var output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                output.setRGB(x, y, image.getPixelReader().getArgb(x, y));

        ImageIO.write(output, "png", new File(file));
        System.out.println("SELFTEST picture " + file);
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
        if (shot != null)
            writePicture(shot);

        Platform.exit();
    }
}
