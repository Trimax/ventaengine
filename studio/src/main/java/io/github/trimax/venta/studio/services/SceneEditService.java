package io.github.trimax.venta.studio.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.function.Consumer;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import com.google.gson.JsonObject;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.studio.StudioSession;
import io.github.trimax.venta.studio.model.command.Command;
import io.github.trimax.venta.studio.model.command.History;
import io.github.trimax.venta.studio.model.command.StructureCommand;
import io.github.trimax.venta.studio.model.event.OutlineSelectionRequestEvent;
import io.github.trimax.venta.studio.model.outline.OutlineItem;
import io.github.trimax.venta.studio.model.outline.OutlineNode;
import io.github.trimax.venta.studio.utils.NamingUtil;
import io.github.trimax.venta.studio.utils.PlacementUtil;
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
public final class SceneEditService implements ReplaceEntryCommand.Writer, StructureCommand.Store {
    private final StudioSession session;
    private final VentaContext context;
    private final SceneApplier applier;

    /** A copy is put this far from the thing it is made of (meters), so it is seen */
    private static final float DUPLICATE_OFFSET = 1.f;

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

    /**
     * Puts a thing made of a file of the project in the scene, at the end of the things of its kind, and chooses it.
     *
     * @param resource the file of the thing as the scene names it, like {@code trees/pine.json}
     * @return whether the thing is in the scene: it is not when the file can't be loaded
     */
    public boolean create(@NonNull final OutlineKind kind, @NonNull final String resource, @NonNull final Vector3fc position) {
        if (document == null)
            return false;

        final var name = NamingUtil.unique(namesOf(kind), NamingUtil.nameOf(resource));
        return perform(StructureCommand.insertion(this, kind, document.count(kind), PlacementUtil.entryFor(kind, resource, name, position), "Create " + name));
    }

    /** Makes a copy of a thing: it is the next one of its kind, one meter aside, and has a name of its own */
    public boolean duplicate(@NonNull final OutlineKind kind, final int index) {
        if (document == null || index < 0 || index >= document.count(kind))
            return false;

        final var entry = document.getEntry(kind, index);
        final var name = NamingUtil.unique(namesOf(kind), EntryUtil.getName(entry));
        EntryUtil.setName(entry, name);
        EntryUtil.setVector(entry, EntryUtil.POSITION, EntryUtil.getVector(entry, EntryUtil.POSITION, new Vector3f()).add(DUPLICATE_OFFSET, 0.f, 0.f));

        return perform(StructureCommand.insertion(this, kind, index + 1, entry, "Duplicate " + name));
    }

    /** Takes a thing out of the scene; undo brings it back to its place */
    public boolean delete(@NonNull final OutlineKind kind, final int index) {
        if (document == null || index < 0 || index >= document.count(kind))
            return false;

        final var entry = document.getEntry(kind, index);
        return perform(StructureCommand.removal(this, kind, index, entry, "Delete " + EntryUtil.getName(entry)));
    }

    @Override
    public void insert(final OutlineKind kind, final int index, final JsonObject entry) {
        document.insertEntry(kind, index, entry);

        try {
            applier.create(kind, index, entry);
        } catch (final RuntimeException exception) {
            // The thing can't be made (its file is not loaded): the file of the scene is as it was
            document.removeEntry(kind, index);
            throw exception;
        }

        // The hierarchy that the panels got is the one whose items are chosen: the items are told apart by identity
        EventUtil.post(new OutlineSelectionRequestEvent(OutlineUtil.at(announce(), kind, index)));
    }

    @Override
    public void remove(final OutlineKind kind, final int index) {
        applier.remove(kind, index);
        document.removeEntry(kind, index);

        // Nothing is chosen: what was chosen may be the thing that is gone, or the one that has its place now
        EventUtil.post(new OutlineSelectionRequestEvent(null));
        announce();
    }

    /**
     * A drag begins: all the changes with the same field are one command until {@link #endDrag()}.
     */
    public void beginDrag() {
        history.hold();
    }

    public void endDrag() {
        history.release();
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

        announce();
    }

    /** Does a command that changes what is in the scene; a command that can't be done is not kept, and the user is told */
    private boolean perform(final Command command) {
        try {
            history.perform(command);
            announceHistory();
            return true;
        } catch (final RuntimeException exception) {
            log.error("Can't do `{}`", command.label(), exception);
            EventUtil.post(new StatusEvent("Can't do `%s`: %s", command.label(), exception.getMessage()));
            return false;
        }
    }

    private List<String> namesOf(final OutlineKind kind) {
        return OutlineUtil.items(OutlineUtil.build(document.getName(), document.getScene()), kind).stream().map(OutlineItem::name).toList();
    }

    /** Tells the panels that the scene changed; returns the hierarchy that they got */
    private OutlineNode announce() {
        final var outline = OutlineUtil.build(document.getName(), document.getScene());
        EventUtil.post(new SceneChangedEvent(document.getScene(), outline));

        return outline;
    }

    private void announceHistory() {
        EventUtil.post(new HistoryChangedEvent(history.undoLabel(), history.redoLabel(), isDirty()));
    }
}
