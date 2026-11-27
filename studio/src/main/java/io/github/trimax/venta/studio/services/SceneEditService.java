package io.github.trimax.venta.studio.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

import com.google.gson.JsonObject;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.studio.StudioSession;
import io.github.trimax.venta.studio.model.command.History;
import io.github.trimax.venta.studio.model.command.ReplaceEntryCommand;
import io.github.trimax.venta.studio.model.document.SceneDocument;
import io.github.trimax.venta.studio.model.event.HistoryChangedEvent;
import io.github.trimax.venta.studio.model.event.SceneChangedEvent;
import io.github.trimax.venta.studio.model.event.StatusEvent;
import io.github.trimax.venta.studio.model.outline.OutlineKind;
import io.github.trimax.venta.studio.utils.EntryUtil;
import io.github.trimax.venta.studio.utils.OutlineUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Edits the scene that is open: the file is changed by commands, the engine shows what the file says, and the history can take the
 * commands back. The file is written on request, in one piece: a file that the studio can't finish writing is not left half written.
 */
@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SceneEditService implements ReplaceEntryCommand.Writer {
    private final StudioSession session;
    private final VentaContext context;
    private final SceneApplier applier;

    private final History history = new History();

    /** The scene that is open, null before one is open */
    @Getter
    private SceneDocument document;

    /** A scene is open: it is as the file says, there is nothing to undo */
    public void open(@NonNull final SceneDocument document) {
        this.document = document;
        history.clear();
        announceHistory();
    }

    /** Whether the scene has changes that are not saved */
    public boolean isDirty() {
        return document != null && history.isDirty();
    }

    /**
     * Changes a thing of the scene as a command: the thing is copied, the change is made on the copy, and when the copy differs, the
     * command is done.
     *
     * @param label  what is done, for the menu: {@code Move Chest}
     * @param field  what is changed: the steps of a drag of the same field are one command
     * @param change what is changed on the copy
     */
    public void edit(@NonNull final OutlineKind kind, final int index, @NonNull final String label, @NonNull final String field, @NonNull final Consumer<JsonObject> change) {
        if (document == null)
            return;

        final var before = document.getEntry(kind, index);
        final var after = before.deepCopy();
        change.accept(after);

        if (before.equals(after))
            return;

        history.perform(new ReplaceEntryCommand(this, kind, index, before, after, label + " " + EntryUtil.getName(before), field));
        announceHistory();
    }

    public void undo() {
        history.undo();
        announceHistory();
    }

    public void redo() {
        history.redo();
        announceHistory();
    }

    /** Writes the scene to its file, the file is replaced in one step */
    public boolean save() {
        if (document == null || session.getProject() == null)
            return false;

        final var file = session.getProject().resolve("scenes").resolve(document.getName());
        try {
            final var temporary = file.resolveSibling(file.getFileName() + ".saving");
            Files.writeString(temporary, document.toJson(), StandardCharsets.UTF_8);
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (final IOException exception) {
            log.error("Can't save the scene {}", file, exception);
            EventUtil.post(new StatusEvent("Can't save the scene %s: %s", document.getName(), exception.getMessage()));
            return false;
        }

        // The scene is made from its file the next time it is open
        context.getSceneRepository().forget(document.getName());

        history.markSaved();
        announceHistory();
        EventUtil.post(new StatusEvent("Scene %s is saved", document.getName()));
        return true;
    }

    /** What the commands write with: the file is changed, and the scene of the engine and the panels show it */
    @Override
    public void write(final OutlineKind kind, final int index, final JsonObject entry) {
        document.setEntry(kind, index, entry);
        applier.apply(kind, index, entry);

        EventUtil.post(new SceneChangedEvent(document.getScene(), OutlineUtil.build(document.getName(), document.getScene())));
    }

    private void announceHistory() {
        EventUtil.post(new HistoryChangedEvent(history.undoLabel(), history.redoLabel(), isDirty()));
    }
}
