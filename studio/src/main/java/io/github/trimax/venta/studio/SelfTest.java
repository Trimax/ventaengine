package io.github.trimax.venta.studio;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import io.github.trimax.venta.container.VentaApplication;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.studio.model.event.OutlineSelectionRequestEvent;
import io.github.trimax.venta.studio.model.outline.OutlineKind;
import io.github.trimax.venta.studio.services.PlacementService;
import io.github.trimax.venta.studio.services.AssetService;
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
    private double[] gizmoFrom;

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
            // The keys fly the camera only while the secondary button is down
            Event.fireEvent(viewport, mouse(MouseEvent.MOUSE_PRESSED, 100, 100));
            viewport.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "d", "d", KeyCode.D, false, false, false, false));
            Event.fireEvent(viewport, mouse(MouseEvent.MOUSE_DRAGGED, 140, 100));
        }

        if (frame == 10 && System.getProperty("studio.tool") != null)
            VentaApplication.getComponent(io.github.trimax.venta.studio.services.GizmoService.class)
                    .setMode(io.github.trimax.venta.engine.model.common.gizmo.GizmoMode.valueOf(System.getProperty("studio.tool")));

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

        if (frame == 25) {
            viewport.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, "d", "d", KeyCode.D, false, false, false, false));
            Event.fireEvent(viewport, mouse(MouseEvent.MOUSE_RELEASED, 140, 100));
        }

        if (System.getProperty("studio.section") != null)
            section();

        if (System.getProperty("studio.resource") != null)
            resource();

        if (System.getProperty("studio.recreate") != null)
            recreate();

        if (System.getProperty("studio.assets") != null)
            assets();

        if (System.getProperty("studio.material") != null)
            material();

        if (System.getProperty("studio.ground") != null)
            ground();

        if (System.getProperty("studio.lighthandle") != null)
            lightHandle();

        if (System.getProperty("studio.gizmo") != null)
            gizmo();

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

        java.util.stream.Stream.of(OutlineKind.values()).flatMap(kind -> OutlineUtil.items(outline, kind).stream())
                .filter(item -> item.name().equals(name)).findFirst()
                .ifPresent(item -> EventUtil.post(new OutlineSelectionRequestEvent(item)));
    }

    /**
     * Takes the x handle of the gizmo of the chosen object with the primary button, drags it in three steps, lets it go, and undoes it:
     * the handle is found by looking for a point of the viewport that the engine says is on it, the mouse events are the real ones.
     */
    private void gizmo() {
        final var edits = VentaApplication.getComponent(SceneEditService.class);
        final var selection = VentaApplication.getComponent(SelectionService.class);
        final var engine = VentaApplication.getComponent(io.github.trimax.venta.engine.core.Engine.class);
        final var overlay = VentaApplication.getComponent(StudioSession.class).getOverlay();
        final var item = selection.getSelected();
        if (item == null)
            return;

        final var width = viewport.getWidth();
        final var height = viewport.getHeight();

        if (frame == 50) {
            System.out.printf("SELFTEST gizmo: mode=%s, origin=%s, x=%.2f%n", overlay.getGizmoMode(), overlay.getGizmoOrigin(), position(edits, item).x);

            for (int row = 1; row < 100 && gizmoFrom == null; row++)
                for (int column = 1; column < 100 && gizmoFrom == null; column++) {
                    final var x = column / 100.f;
                    final var y = row / 100.f;
                    if (io.github.trimax.venta.engine.utils.GizmoUtil.hit(overlay.getGizmoMode(), overlay.getGizmoOrigin(), overlay.getGizmoAxes(),
                            engine.rayThrough(x, y), flyCamera.getPosition()) == io.github.trimax.venta.engine.model.common.gizmo.GizmoHandle.AxisX)
                        gizmoFrom = new double[]{x * width, y * height};
                }

            System.out.println("SELFTEST gizmo: x handle at " + (gizmoFrom == null ? "none" : (int) gizmoFrom[0] + "," + (int) gizmoFrom[1]));
            if (gizmoFrom != null)
                Event.fireEvent(viewport, primary(MouseEvent.MOUSE_PRESSED, gizmoFrom[0], gizmoFrom[1]));
        }

        if (gizmoFrom != null && frame >= 51 && frame <= 53)
            Event.fireEvent(viewport, primary(MouseEvent.MOUSE_DRAGGED, gizmoFrom[0] + (frame - 50) * 15, gizmoFrom[1]));

        if (gizmoFrom != null && frame == 54) {
            Event.fireEvent(viewport, primary(MouseEvent.MOUSE_RELEASED, gizmoFrom[0] + 45, gizmoFrom[1]));
            System.out.printf("SELFTEST gizmo: dragged x=%.2f, dirty=%s%n", position(edits, item).x, edits.isDirty());
        }

        if (gizmoFrom != null && frame == 60) {
            edits.undo();
            System.out.printf("SELFTEST gizmo: undone x=%.2f, dirty=%s (one step)%n", position(edits, item).x, edits.isDirty());
        }
    }

    /** Changes the fog and the sun through the editor of the scene, undoes it and does it again: the numbers of the file and of the engine */
    private void section() {
        final var edits = VentaApplication.getComponent(SceneEditService.class);
        final var scene = VentaApplication.getComponent(io.github.trimax.venta.engine.context.VentaContext.class).getSceneManager().getCurrent();

        if (frame == 50) {
            System.out.printf("SELFTEST section: before: fog density=%.4f, sun intensity=%.2f%n", scene.getFog().getDensity(), scene.getDirectionalLight().getIntensity());
            edits.editSection("Fog", "Change", "density", fog -> fog.addProperty("density", 0.02));
            edits.editSection("Sun", "Change", "intensity", sun -> sun.addProperty("intensity", 3.0));
            System.out.printf("SELFTEST section: changed: fog density=%.4f, sun intensity=%.2f, dirty=%s%n", scene.getFog().getDensity(), scene.getDirectionalLight().getIntensity(), edits.isDirty());
        }

        if (frame == 60) {
            edits.undo();
            edits.undo();
            System.out.printf("SELFTEST section: undone: fog density=%.4f, sun intensity=%.2f, dirty=%s%n", scene.getFog().getDensity(), scene.getDirectionalLight().getIntensity(), edits.isDirty());
        }

        if (frame == 70) {
            edits.redo();
            System.out.printf("SELFTEST section: redone: fog density=%.4f%n", scene.getFog().getDensity());
        }
    }

    /** Changes the file of a light and the skybox, undoes it, does it again and saves: the numbers of the engine and of the file on the disk */
    private void resource() {
        final var edits = VentaApplication.getComponent(SceneEditService.class);
        final var applier = VentaApplication.getComponent(SceneApplier.class);
        final var scene = VentaApplication.getComponent(io.github.trimax.venta.engine.context.VentaContext.class).getSceneManager().getCurrent();
        final var path = System.getProperty("studio.resource");
        final var light = (io.github.trimax.venta.engine.model.instance.LightInstance) applier.instanceOf(OutlineKind.Light, 0);

        if (frame == 50) {
            System.out.printf("SELFTEST resource: before: range=%.1f, intensity=%.2f, skybox=%s, uses=%d%n", light.getRange(), light.getIntensity(), scene.getSkybox().getID(),
                    edits.usesOf(OutlineKind.Light, path.substring(path.lastIndexOf('/') + 1)));
            edits.editResource(path, "Change", "range", file -> file.addProperty("range", 12.0));
            edits.editResource(path, "Change", "intensity", file -> file.addProperty("intensity", 9.0));
            edits.editSection("Skybox", "Change", "cubemap", skybox -> skybox.addProperty("cubemap", "stars.json"));
            System.out.printf("SELFTEST resource: changed: range=%.1f, intensity=%.2f, skybox=%s, dirty=%s%n", light.getRange(), light.getIntensity(), scene.getSkybox().getID(), edits.isDirty());
            edits.editResource(path, "Change", "range", file -> file.addProperty("range", -5.0));
            System.out.printf("SELFTEST resource: refused a negative range: range=%.1f%n", light.getRange());
        }

        if (frame == 60) {
            edits.undo();
            edits.undo();
            edits.undo();
            System.out.printf("SELFTEST resource: undone: range=%.1f, intensity=%.2f, skybox=%s, dirty=%s%n", light.getRange(), light.getIntensity(), scene.getSkybox().getID(), edits.isDirty());
        }

        if (frame == 70) {
            edits.redo();
            edits.redo();
            edits.redo();
            final var saved = edits.save();
            System.out.printf("SELFTEST resource: redone and saved=%s: range=%.1f, dirty=%s%n", saved, light.getRange(), edits.isDirty());
        }
    }

    /** Takes the handle of the range of the chosen light with the primary button, drags it in three steps, lets it go, and undoes it */
    private void lightHandle() {
        final var edits = VentaApplication.getComponent(SceneEditService.class);
        final var engine = VentaApplication.getComponent(io.github.trimax.venta.engine.core.Engine.class);
        final var applier = VentaApplication.getComponent(SceneApplier.class);
        final var light = (io.github.trimax.venta.engine.model.instance.LightInstance) applier.instanceOf(OutlineKind.Light, 0);
        final var overlay = VentaApplication.getComponent(StudioSession.class).getOverlay();

        if (frame == 50) {
            final var handles = io.github.trimax.venta.engine.utils.LightShapeUtil.handles(light);
            System.out.printf("SELFTEST lighthandle: range=%.1f, handles=%d%n", light.getRange(), handles.size());

            for (int row = 1; row < 100 && gizmoFrom == null; row++)
                for (int column = 1; column < 100 && gizmoFrom == null; column++) {
                    final var hit = io.github.trimax.venta.engine.utils.LightShapeUtil.hit(handles, engine.rayThrough(column / 100.f, row / 100.f), flyCamera.getPosition());
                    if (hit != null && hit.index() == 0)
                        gizmoFrom = new double[]{column / 100.f * viewport.getWidth(), row / 100.f * viewport.getHeight()};
                }

            System.out.println("SELFTEST lighthandle: x handle at " + (gizmoFrom == null ? "none" : (int) gizmoFrom[0] + "," + (int) gizmoFrom[1]));
            if (gizmoFrom != null)
                Event.fireEvent(viewport, primary(MouseEvent.MOUSE_PRESSED, gizmoFrom[0], gizmoFrom[1]));
        }

        if (gizmoFrom != null && frame >= 51 && frame <= 53)
            Event.fireEvent(viewport, primary(MouseEvent.MOUSE_DRAGGED, gizmoFrom[0] - (frame - 50) * 10, gizmoFrom[1]));

        if (gizmoFrom != null && frame == 54) {
            Event.fireEvent(viewport, primary(MouseEvent.MOUSE_RELEASED, gizmoFrom[0] - 30, gizmoFrom[1]));
            System.out.printf("SELFTEST lighthandle: dragged range=%.1f, dirty=%s, lit=%d%n", light.getRange(), edits.isDirty(), overlay.getLightHandle());
        }

        if (gizmoFrom != null && frame == 60) {
            edits.undo();
            System.out.printf("SELFTEST lighthandle: undone range=%.1f, dirty=%s (one step)%n", light.getRange(), edits.isDirty());
        }
    }

    /** Changes the file of an emitter and of an object (the things are made again), tries a file that can't be loaded, undoes and saves */
    private void recreate() {
        final var edits = VentaApplication.getComponent(SceneEditService.class);
        final var applier = VentaApplication.getComponent(SceneApplier.class);
        final var scene = VentaApplication.getComponent(io.github.trimax.venta.engine.context.VentaContext.class).getSceneManager().getCurrent();
        final var path = "emitters/campfire-flame.json";
        final var emitter = applier.instanceOf(OutlineKind.Emitter, 0);
        final var smoke = applier.instanceOf(OutlineKind.Emitter, 1);

        if (frame == 50) {
            System.out.printf("SELFTEST recreate: before: emitters=%d, first=%s%n", scene.getEmitters().size(), ((io.github.trimax.venta.engine.model.instance.EmitterInstance) emitter).getName());
            edits.editResource(path, "Change", "emissionRate", file -> file.addProperty("emissionRate", 10));
            final var again = applier.instanceOf(OutlineKind.Emitter, 0);
            System.out.printf("SELFTEST recreate: changed: emitters=%d, made again=%s, name=%s, second the same=%s, dirty=%s%n", scene.getEmitters().size(), again != emitter,
                    ((io.github.trimax.venta.engine.model.instance.EmitterInstance) again).getName(), applier.instanceOf(OutlineKind.Emitter, 1) == smoke, edits.isDirty());
            edits.editResource(path, "Change", "texture", file -> file.addProperty("texture", "sprites/nothing.png"));
            System.out.printf("SELFTEST recreate: refused a texture that is not there: emitters=%d, dirty=%s%n", scene.getEmitters().size(), edits.isDirty());
        }

        if (frame == 60) {
            edits.undo();
            System.out.printf("SELFTEST recreate: undone: emitters=%d, dirty=%s%n", scene.getEmitters().size(), edits.isDirty());
        }

        if (frame == 70) {
            edits.redo();
            final var saved = edits.save();
            System.out.printf("SELFTEST recreate: redone and saved=%s, emitters=%d, dirty=%s%n", saved, scene.getEmitters().size(), edits.isDirty());
        }
    }

    /** Clicks on the ground and on the water, and drops a file on the ground: what is chosen, and where the thing lands */
    private void ground() {
        final var selection = VentaApplication.getComponent(SelectionService.class);
        final var session = VentaApplication.getComponent(StudioSession.class);
        final var edits = VentaApplication.getComponent(SceneEditService.class);
        final var values = System.getProperty("studio.ground").split(",");
        final var x = Float.parseFloat(values[0]);
        final var y = Float.parseFloat(values[1]);

        if (frame == 50) {
            selection.pick(x, y);
            final var item = selection.getSelected();
            System.out.printf("SELFTEST ground: clicked (%.2f, %.2f): %s%n", x, y, item == null ? "nothing" : item.kind() + " " + item.name());
        }

        if (frame == 60) {
            final var before = edits.getDocument().count(OutlineKind.Object);
            VentaApplication.getComponent(PlacementService.class).place(session.getProject().resolve(values[2]), x, y);
            final var entry = edits.getDocument().getEntry(OutlineKind.Object, before);
            System.out.printf("SELFTEST ground: dropped at %s%n", EntryUtil.getVector(entry, EntryUtil.POSITION, new org.joml.Vector3f()));
        }
    }

    /** Changes the file of a material that an object uses (the objects that use it are made again), tries a texture that isn't there, undoes and saves */
    private void material() {
        final var edits = VentaApplication.getComponent(SceneEditService.class);
        final var applier = VentaApplication.getComponent(SceneApplier.class);
        final var scene = VentaApplication.getComponent(io.github.trimax.venta.engine.context.VentaContext.class).getSceneManager().getCurrent();
        final var path = System.getProperty("studio.material");
        final var chest = applier.instanceOf(OutlineKind.Object, 0);
        final var second = applier.instanceOf(OutlineKind.Object, 1);

        if (frame == 50) {
            System.out.printf("SELFTEST material: before: objects=%d, first=%s%n", scene.getObjects().size(), ((io.github.trimax.venta.engine.model.instance.ObjectInstance) chest).getName());
            edits.editResource(path, "Change", "roughness", file -> file.addProperty("roughness", 0.9));
            final var again = applier.instanceOf(OutlineKind.Object, 0);
            System.out.printf("SELFTEST material: changed: objects=%d, made again=%s, name=%s, second the same=%s, dirty=%s%n", scene.getObjects().size(), again != chest,
                    ((io.github.trimax.venta.engine.model.instance.ObjectInstance) again).getName(), applier.instanceOf(OutlineKind.Object, 1) == second, edits.isDirty());
            edits.editResource(path, "Change", "diffuse", file -> file.getAsJsonObject("textures").addProperty("diffuse", "chest/nothing.png"));
            System.out.printf("SELFTEST material: refused a texture that is not there: objects=%d%n", scene.getObjects().size());
        }

        if (frame == 60) {
            edits.undo();
            System.out.printf("SELFTEST material: undone: objects=%d, dirty=%s%n", scene.getObjects().size(), edits.isDirty());
        }

        if (frame == 70) {
            edits.redo();
            System.out.printf("SELFTEST material: redone and saved=%s, objects=%d%n", edits.save(), scene.getObjects().size());
        }
    }

    /** Brings a file into the project and renames an object file that the scene names: the scene is read again with the new name */
    @SneakyThrows
    private void assets() {
        final var assets = VentaApplication.getComponent(AssetService.class);
        final var edits = VentaApplication.getComponent(SceneEditService.class);
        final var session = VentaApplication.getComponent(StudioSession.class);
        final var scene = VentaApplication.getComponent(io.github.trimax.venta.engine.context.VentaContext.class).getSceneManager().getCurrent();

        if (frame == 50) {
            final var outside = java.nio.file.Files.createTempFile("studio-import", ".png");
            final var imported = assets.importFiles(java.util.List.of(outside), null);
            System.out.println("SELFTEST assets: imported " + imported.stream().map(path -> session.getProject().relativize(path).getParent()).toList());
            java.nio.file.Files.delete(outside);

            final var old = session.getProject().resolve("objects/chest-beach.json");
            System.out.println("SELFTEST assets: renamed=" + assets.rename(old, "crate.json", false));
            System.out.printf("SELFTEST assets: objects=%d, entry names `%s`, scene=%s, file moved=%s%n", scene.getObjects().size(),
                    edits.getDocument().getEntry(OutlineKind.Object, 0).get("object").getAsString(), session.getScene(), java.nio.file.Files.exists(session.getProject().resolve("objects/crate.json")));
        }

        if (frame == 60) {
            final var now = VentaApplication.getComponent(io.github.trimax.venta.engine.context.VentaContext.class).getSceneManager().getCurrent();
            System.out.printf("SELFTEST assets: after reopening objects=%d, dirty=%s%n", now.getObjects().size(), edits.isDirty());
        }
    }

    private org.joml.Vector3f position(final SceneEditService edits, final io.github.trimax.venta.studio.model.outline.OutlineItem item) {
        return EntryUtil.getVector(edits.getDocument().getEntry(item.kind(), item.index()), EntryUtil.POSITION, new org.joml.Vector3f());
    }

    /** A button of the mouse at a point of the viewport (in its own pixels) */
    private MouseEvent primary(final EventType<MouseEvent> type, final double x, final double y) {
        final var point = viewport.localToScene(x, y);
        return new MouseEvent(type, point.getX(), point.getY(), point.getX(), point.getY(), MouseButton.PRIMARY, 1, false, false, false, false,
                type != MouseEvent.MOUSE_RELEASED, false, false, false, false, false, null);
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
