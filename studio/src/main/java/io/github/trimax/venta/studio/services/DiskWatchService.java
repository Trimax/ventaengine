package io.github.trimax.venta.studio.services;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.common.eventbus.Subscribe;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.studio.StudioSession;
import io.github.trimax.venta.studio.model.event.FilesWrittenEvent;
import io.github.trimax.venta.studio.model.event.ProjectOpenedEvent;
import io.github.trimax.venta.studio.model.event.StatusEvent;
import io.github.trimax.venta.studio.utils.DiskUtil;
import io.github.trimax.venta.studio.utils.ResourceKindUtil;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Notices that files of the project were changed by another program (when the window is entered again) and shows the scene as the files
 * say now: the things that are made of a changed file are made again, and the scene itself is read again when its file changed.
 */
@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DiskWatchService {
    private final StudioSession session;
    private final SceneEditService edits;
    private final SceneService scenes;

    /** The files of the project and when they were changed, as the studio knows them */
    private Map<Path, Long> known = new HashMap<>();

    @Subscribe
    public void onProjectOpened(final ProjectOpenedEvent event) {
        sync();
    }

    /** The studio wrote files itself: they are what the studio knows, not a change from outside */
    @Subscribe
    public void onFilesWritten(final FilesWrittenEvent event) {
        sync();
    }

    /** Takes what the files are now as what the studio knows */
    public void sync() {
        known = session.getProject() == null ? new HashMap<>() : DiskUtil.snapshot(session.getProject());
    }

    /**
     * Looks for the files that were changed since the studio last looked, and shows them.
     *
     * @return how many files were changed
     */
    public int check() {
        final var project = session.getProject();
        if (project == null)
            return 0;

        final var current = DiskUtil.snapshot(project);
        final var changed = DiskUtil.changed(known, current);
        known = current;

        if (changed.isEmpty() || edits.getDocument() == null)
            return changed.size();

        var sceneChanged = false;
        final var kept = new ArrayList<String>();
        for (final var file : changed) {
            final var path = project.relativize(file).toString().replace('\\', '/');
            sceneChanged |= path.equals("scenes/" + session.getScene());

            try {
                if (!reload(path))
                    kept.add(path);
            } catch (final RuntimeException exception) {
                log.error("Can't show the change of {}", path, exception);
                EventUtil.post(new StatusEvent("Can't show the change of %s: %s", path, exception.getMessage()));
            }
        }

        if (!kept.isEmpty())
            EventUtil.post(new StatusEvent("%s changed on the disk and has changes here: kept as it is here", String.join(", ", kept)));

        if (sceneChanged && (!edits.isDirty() || reloadAsked()))
            scenes.reload(session.getScene());

        EventUtil.post(new StatusEvent("%d files changed on the disk", changed.size()));
        return changed.size();
    }

    /** Shows a file that changed; false when it has changes in the studio that are not saved (it is kept as it is) */
    private boolean reload(final String path) {
        if (ResourceKindUtil.materialOf(path).isPresent() || ResourceKindUtil.of(path).isPresent())
            return edits.reloadFromDisk(path);

        if (path.startsWith("textures/")) {
            edits.reloadTexture(path.substring("textures/".length()));
            return true;
        }

        return true;
    }

    /** The scene file was changed from outside while the scene has changes of its own: asks which of them to keep */
    private boolean reloadAsked() {
        final var reload = new ButtonType("Reload");
        final var keep = new ButtonType("Keep My Changes");
        final var alert = new Alert(Alert.AlertType.CONFIRMATION, "", reload, keep);
        alert.setTitle("Scene changed on the disk");
        alert.setHeaderText(session.getScene() + " was changed by another program");
        alert.setContentText("The changes made here are lost if the scene is read again; if they are kept, saving replaces the file.");
        alert.initOwner(session.getStage());

        return alert.showAndWait().orElse(keep) == reload;
    }
}
