package io.github.trimax.venta.studio.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.studio.StudioSession;
import io.github.trimax.venta.studio.model.event.StatusEvent;
import io.github.trimax.venta.studio.utils.ImportUtil;
import io.github.trimax.venta.studio.utils.ProjectUtil;
import io.github.trimax.venta.studio.utils.RenameUtil;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The files of the project as assets: files from outside are brought in, and a file is renamed with the places that name it.
 */
@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AssetService {
    /** How many of the files that a rename changes are listed in the question */
    private static final int LISTED = 12;

    private final StudioSession session;
    private final SceneService scenes;
    private final UnsavedChangesService unsaved;

    /**
     * Copies files into the project: a file never replaces another, it gets a number in its name when the name is taken.
     *
     * @param folder the folder that is chosen in the project, null for none; for the whole project a file goes to the folder of its kind
     * @return the files that are in the project now
     */
    public List<Path> importFiles(@NonNull final List<Path> sources, final Path folder) {
        final var project = session.getProject();
        if (project == null) {
            EventUtil.post(new StatusEvent("Open a project first"));
            return List.of();
        }

        final var imported = new ArrayList<Path>();
        for (final var source : sources) {
            if (!Files.isRegularFile(source)) {
                EventUtil.post(new StatusEvent("%s is not a file, folders are not imported", source.getFileName()));
                continue;
            }

            if (source.toAbsolutePath().normalize().startsWith(project.toAbsolutePath().normalize()))
                continue;

            try {
                final var target = ImportUtil.folderFor(project, folder, source.getFileName().toString());
                Files.createDirectories(target);

                final var destination = target.resolve(ImportUtil.uniqueName(target, source.getFileName().toString(), Files::exists));
                Files.copy(source, destination, StandardCopyOption.COPY_ATTRIBUTES);
                imported.add(destination);
            } catch (final IOException exception) {
                log.error("Can't import {}", source, exception);
                EventUtil.post(new StatusEvent("Can't import %s: %s", source.getFileName(), exception.getMessage()));
            }
        }

        if (!imported.isEmpty())
            EventUtil.post(new StatusEvent("Imported %s", imported.size() == 1 ? project.relativize(imported.getFirst()).toString().replace('\\', '/') : imported.size() + " files"));

        return imported;
    }

    /** Asks which files to bring into the project, and brings them */
    public List<Path> chooseAndImport(final Path folder) {
        final var chooser = new FileChooser();
        chooser.setTitle("Import Files");

        final var chosen = chooser.showOpenMultipleDialog(session.getStage());
        return chosen == null ? List.of() : importFiles(chosen.stream().map(java.io.File::toPath).toList(), folder);
    }

    /**
     * Asks for a new name of a file and renames it: the places of the project that name the file get the new name, after the user has
     * seen which files they are in. The scene that is open is made again from its file.
     *
     * @return whether the file was renamed
     */
    public boolean rename(@NonNull final Path file) {
        final var project = session.getProject();
        if (project == null)
            return false;

        final var dialog = new TextInputDialog(file.getFileName().toString());
        dialog.setTitle("Rename");
        dialog.setHeaderText("Rename " + project.relativize(file).toString().replace('\\', '/'));
        dialog.setContentText("Name:");
        dialog.initOwner(session.getStage());

        final var name = dialog.showAndWait().orElse(null);
        return name != null && rename(file, name, true);
    }

    /**
     * Renames a file.
     *
     * @param ask whether the user is shown which files are changed and asked to go on (when there are any)
     * @return whether the file was renamed
     */
    public boolean rename(@NonNull final Path file, @NonNull final String name, final boolean ask) {
        final var project = session.getProject();
        if (project == null)
            return false;

        // The scene is saved (or its changes are let go) before the files are read, so what is changed is what is on the disk
        if (!unsaved.confirm())
            return false;

        final RenameUtil.Plan plan;
        try {
            plan = RenameUtil.plan(project, file, name);
        } catch (final IllegalArgumentException exception) {
            EventUtil.post(new StatusEvent("Can't rename: %s", exception.getMessage()));
            return false;
        }

        if (ask && plan.references() > 0 && !confirmed(project, plan))
            return false;

        try {
            RenameUtil.apply(plan);
        } catch (final IOException | RuntimeException exception) {
            log.error("Can't rename {}", file, exception);
            EventUtil.post(new StatusEvent("Can't rename %s: %s", file.getFileName(), exception.getMessage()));
            return false;
        }

        reopenScene(project, plan);
        EventUtil.post(new StatusEvent("Renamed %s to %s, %d places changed", file.getFileName(), plan.to().getFileName(), plan.references()));
        return true;
    }

    /** Shows which files a rename changes, and asks whether to go on */
    private boolean confirmed(final Path project, final RenameUtil.Plan plan) {
        final var text = new StringBuilder();
        plan.changes().stream().limit(LISTED).forEach(change -> text.append(project.relativize(change.file()).toString().replace('\\', '/'))
                .append(change.count() > 1 ? " (" + change.count() + " places)" : "").append('\n'));
        if (plan.changes().size() > LISTED)
            text.append("... and ").append(plan.changes().size() - LISTED).append(" more\n");

        final var alert = new Alert(Alert.AlertType.CONFIRMATION, text.toString(), ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Rename");
        alert.setHeaderText(plan.references() + " places in " + plan.changes().size() + " files name " + plan.from().getFileName() + " and get the new name");
        alert.initOwner(session.getStage());

        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    /** The scene that is open was read before the rename: it is read again, and a scene that was renamed is open under its new name */
    private void reopenScene(final Path project, final RenameUtil.Plan plan) {
        final var open = session.getScene();
        if (open == null)
            return;

        final var renamed = ProjectUtil.groupOf(project, plan.from()) == GroupType.Scene ? ProjectUtil.sceneNameOf(project, plan.from()) : null;
        scenes.reload(open.equals(renamed) ? ProjectUtil.sceneNameOf(project, plan.to()) : open);
    }
}
