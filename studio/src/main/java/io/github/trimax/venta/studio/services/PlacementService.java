package io.github.trimax.venta.studio.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.utils.RayUtil;
import io.github.trimax.venta.studio.StudioSession;
import io.github.trimax.venta.studio.model.event.StatusEvent;
import io.github.trimax.venta.studio.model.outline.OutlineKind;
import io.github.trimax.venta.studio.utils.PlacementUtil;
import javafx.scene.control.ChoiceDialog;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Puts the files of the project in the scene that is open: the file is chosen in a list (the menu) or dragged from the project panel into
 * the viewport (it goes where the mouse is dropped) or into the hierarchy.
 */
@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PlacementService {
    private static final Vector3f UP = new Vector3f(0.f, 1.f, 0.f);

    private final StudioSession session;
    private final SceneEditService edits;
    private final Engine engine;

    /** Whether a file of the project can be put in a scene: it is a file of a thing that a scene holds */
    public boolean isPlaceable(final Path file) {
        return session.getProject() != null && file != null && PlacementUtil.kindOf(session.getProject(), file).isPresent();
    }

    /**
     * Asks which file of the kind to put in the scene, and puts it where the camera turns round.
     */
    public void chooseAndPlace(@NonNull final OutlineKind kind) {
        if (!ready())
            return;

        final var files = files(kind);
        if (files.isEmpty()) {
            EventUtil.post(new StatusEvent("The project has no files of %s", PlacementUtil.groupOf(kind).getDisplayName().toLowerCase()));
            return;
        }

        final var dialog = new ChoiceDialog<>(files.getFirst(), files);
        dialog.setTitle("Create");
        dialog.setHeaderText(PlacementUtil.groupOf(kind).getDisplayName());
        dialog.setContentText("File:");
        dialog.initOwner(session.getStage());

        dialog.showAndWait().ifPresent(resource -> edits.create(kind, resource, pivot()));
    }

    /**
     * Puts a file of the project in the scene.
     *
     * @param x where it is dropped in the viewport, from 0 (the left edge) to 1 (the right one); null for the place that the camera turns round
     * @param y from 0 (the top edge) to 1 (the bottom one)
     * @return whether the thing is in the scene
     */
    public boolean place(@NonNull final Path file, final Float x, final Float y) {
        if (!ready())
            return false;

        final Optional<OutlineKind> kind = PlacementUtil.kindOf(session.getProject(), file);
        if (kind.isEmpty()) {
            EventUtil.post(new StatusEvent("%s is not a thing that a scene holds", file.getFileName()));
            return false;
        }

        return edits.create(kind.get(), PlacementUtil.resourceOf(session.getProject(), file), x == null || y == null ? pivot() : ground(x, y));
    }

    /** The point of the plane at the height of the pivot that is under a point of the viewport, the pivot when the point is above the horizon */
    private Vector3fc ground(final float x, final float y) {
        final var ray = engine.rayThrough(x, y);
        final var pivot = pivot();

        final var distance = RayUtil.intersectPlane(ray, pivot, UP);
        return distance == RayUtil.MISS ? pivot : new Vector3f(ray.origin()).fma(distance, ray.direction());
    }

    private Vector3fc pivot() {
        return new Vector3f(session.getCamera().getPivot());
    }

    private boolean ready() {
        if (session.getProject() != null && edits.getDocument() != null)
            return true;

        EventUtil.post(new StatusEvent("Open a scene first"));
        return false;
    }

    /** The files of the group of the kind, as the scene names them */
    private List<String> files(final OutlineKind kind) {
        final var folder = session.getProject().resolve(PlacementUtil.groupOf(kind).getArchiveFolder());
        if (!Files.isDirectory(folder))
            return List.of();

        try (final Stream<Path> found = Files.walk(folder)) {
            return found.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(".json"))
                    .map(file -> PlacementUtil.resourceOf(session.getProject(), file))
                    .sorted()
                    .toList();
        } catch (final IOException exception) {
            log.warn("Can't list {}", folder, exception);
            return List.of();
        }
    }
}
