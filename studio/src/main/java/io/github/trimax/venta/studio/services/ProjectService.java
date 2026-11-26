package io.github.trimax.venta.studio.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.studio.StudioLauncher;
import io.github.trimax.venta.studio.StudioSession;
import io.github.trimax.venta.studio.model.event.ProjectOpenedEvent;
import io.github.trimax.venta.studio.model.event.StatusEvent;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Opens the project. The engine keeps what it has loaded by the names of the files, and can't forget it safely, so a window has one
 * project for its life: opening another one starts another window.
 */
@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProjectService {
    private final StudioSession session;
    private final VentaContext context;

    private final SettingsStore store = new SettingsStore(System.getProperty("studio.settings") == null ? SettingsStore.defaultFile() : Path.of(System.getProperty("studio.settings")));

    /** The settings of the previous runs go to the session: the studio asks for them before it opens anything */
    public void loadSettings() {
        session.setSettings(store.load());
    }

    public void saveWindow(final int width, final int height) {
        session.setSettings(session.getSettings().withWindow(width, height));
        store.save(session.getSettings());
    }

    /**
     * Opens the project of a folder here, or in a new window when a project is open here already.
     *
     * @return whether the project was opened (or the window for it started)
     */
    public boolean open(@NonNull final Path folder) {
        if (!Files.isDirectory(folder)) {
            EventUtil.post(new StatusEvent("The folder %s doesn't exist", folder));
            session.setSettings(session.getSettings().withoutProject(folder.toString()));
            store.save(session.getSettings());
            return false;
        }

        final var normalized = folder.toAbsolutePath().normalize();
        remember(normalized);

        if (session.getProject() != null)
            return openInNewWindow(normalized);

        context.useProject(normalized);
        session.setProject(normalized);
        EventUtil.post(new ProjectOpenedEvent(normalized));
        EventUtil.post(new StatusEvent("Project %s is open", normalized.getFileName()));

        return true;
    }

    private void remember(final Path folder) {
        session.setSettings(session.getSettings().withProject(folder.toString()));
        store.save(session.getSettings());
    }

    /** The studio is started again with the same classes: from a jar and from the classes of an IDE alike */
    private boolean openInNewWindow(final Path folder) {
        final var command = new ArrayList<String>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add(StudioLauncher.class.getName());
        command.add(StudioLauncher.PROJECT_ARGUMENT);
        command.add(folder.toString());

        try {
            new ProcessBuilder(command).directory(new File(System.getProperty("user.dir"))).inheritIO().start();
            EventUtil.post(new StatusEvent("Project %s is opened in a new window", folder.getFileName()));
            return true;
        } catch (final IOException exception) {
            log.error("Can't start a window for the project {}", folder, exception);
            EventUtil.post(new StatusEvent("Can't start a window for the project: %s", exception.getMessage()));
            return false;
        }
    }
}
