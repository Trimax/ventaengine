package io.github.trimax.venta.studio.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.function.Consumer;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import com.google.gson.JsonElement;
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
import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.studio.utils.ReferenceScanUtil;
import io.github.trimax.venta.studio.utils.ResourceKindUtil;
import io.github.trimax.venta.studio.model.command.ReplaceEntryCommand;
import io.github.trimax.venta.studio.model.command.ReplaceResourceCommand;
import io.github.trimax.venta.studio.model.command.ReplaceSectionCommand;
import io.github.trimax.venta.studio.model.document.ResourceDocument;
import io.github.trimax.venta.studio.model.document.SceneDocument;
import io.github.trimax.venta.studio.model.event.FilesWrittenEvent;
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
public final class SceneEditService implements ReplaceEntryCommand.Writer, ReplaceSectionCommand.Writer, ReplaceResourceCommand.Writer, StructureCommand.Store {
    private final StudioSession session;
    private final VentaContext context;
    private final SceneApplier applier;

    /** A copy is put this far from the thing it is made of (meters), so it is seen */
    private static final float DUPLICATE_OFFSET = 1.f;

    private final History history = new History();

    /** The files of the resources that were opened for editing, by their places in the project: they are read from the disk once */
    private final Map<String, ResourceDocument> resources = new LinkedHashMap<>();

    /** The files that the engine has been shown changed (it made things of them from their edited contents) */
    private final Set<String> overridden = new LinkedHashSet<>();

    /** The scene that is open, null before one is open */
    @Getter
    private SceneDocument document;

    /**
     * The files of the resources that were changed and not written are what the disk has again, and so is what the engine has made of
     * them: a scene that is opened next is made of the files as they are.
     */
    public void discardResources() {
        for (final var path : overridden) {
            final var material = ResourceKindUtil.materialOf(path);
            if (material.isPresent())
                applier.discardMaterial(path, dependentsOf(material.get()));
            else
                applier.discard(path);
        }

        overridden.clear();
        resources.clear();
    }

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
     * Changes a section of the environment (the sun, the fog, ...) as a command, as {@link #edit} does for a thing.
     *
     * @param section the name of the section in the hierarchy
     */
    public void editSection(@NonNull final String section, @NonNull final String label, @NonNull final String field, @NonNull final Consumer<JsonObject> change) {
        if (document == null || !document.hasSection(section))
            return;

        final var before = document.getSection(section);
        final var after = before.deepCopy();
        change.accept(after);

        if (before.equals(after))
            return;

        perform(new ReplaceSectionCommand(this, section, before, after, label + " " + section.toLowerCase(Locale.ROOT), field));
    }

    /**
     * The file of a resource of the project as it is edited now, null when it can't be read.
     *
     * @param path the place of the file in the project, like {@code lights/campfire.json}
     */
    public JsonObject readResource(@NonNull final String path) {
        final var resource = resource(path);
        return resource == null ? null : resource.get();
    }

    /**
     * Changes the file of a resource as a command, as {@link #edit} does for a thing. Every thing of the scene that is made of the file shows
     * the change; the file is written when the scene is saved.
     *
     * @param path the place of the file in the project
     */
    public void editResource(@NonNull final String path, @NonNull final String label, @NonNull final String field, @NonNull final Consumer<JsonObject> change) {
        final var resource = document == null ? null : resource(path);
        if (resource == null)
            return;

        final var before = resource.get();
        final var after = before.deepCopy();
        change.accept(after);

        if (before.equals(after))
            return;

        perform(new ReplaceResourceCommand(this, path, before, after, label + " " + path.substring(path.lastIndexOf('/') + 1), field));
    }

    /** How many things of the scene that is open are made of the file of a resource */
    public int usesOf(@NonNull final OutlineKind kind, @NonNull final String file) {
        if (document == null)
            return 0;

        final var field = PlacementUtil.fileFieldOf(kind);
        var uses = 0;
        for (int index = 0; index < document.count(kind); index++) {
            final var entry = document.getEntry(kind, index);
            if (field != null && entry.has(field) && file.equals(entry.get(field).getAsString()))
                uses++;
        }

        return uses;
    }

    /**
     * The files in a folder of the project and in the folders in it, as the files that name them are written: the path from the folder,
     * with slashes. None when there is no such folder.
     *
     * @param extensions how the names of the files end
     */
    public List<String> filesOf(@NonNull final String folder, @NonNull final List<String> extensions) {
        if (session.getProject() == null || !Files.isDirectory(session.getProject().resolve(folder)))
            return List.of();

        final var root = session.getProject().resolve(folder);
        try (final var files = Files.walk(root)) {
            return files.filter(Files::isRegularFile)
                    .map(file -> root.relativize(file).toString().replace('\\', '/'))
                    .filter(name -> extensions.stream().anyMatch(name::endsWith))
                    .sorted()
                    .toList();
        } catch (final IOException exception) {
            log.warn("Can't list {}", folder, exception);
            return List.of();
        }
    }

    /**
     * A file of a resource (a thing, a material) was changed on the disk: it is read again and shown, as if it were edited.
     *
     * @return false when the file has changes here that are not saved: it is left as it is here
     */
    public boolean reloadFromDisk(@NonNull final String path) {
        if (document == null)
            return true;

        final var known = resources.get(path);
        if (known != null && known.isModified())
            return false;

        resources.remove(path);
        final var fresh = resource(path);
        if (fresh == null)
            return true;

        overridden.add(path);
        show(path, fresh.get());
        announce();
        return true;
    }

    /**
     * A texture was changed on the disk: it is loaded again, and the materials and the things that name it are made again.
     *
     * @param name the file of the texture as the files name it, like {@code chest/diffuse.png}
     */
    public void reloadTexture(@NonNull final String name) {
        if (document == null || session.getProject() == null)
            return;

        applier.forgetTexture(name);

        final var paths = new ArrayList<String>();
        for (final var kind : List.of(OutlineKind.Object, OutlineKind.Emitter, OutlineKind.Billboard, OutlineKind.Terrain, OutlineKind.Water)) {
            final var folder = PlacementUtil.groupOf(kind).getArchiveFolder();
            filesOf(folder, List.of(".json")).forEach(file -> paths.add(folder + "/" + file));
        }

        filesOf("materials", List.of(".json")).forEach(file -> paths.add("materials/" + file));

        // The materials come first: what is made of a thing names a material, and the material has to be the one that is loaded again
        paths.sort(java.util.Comparator.comparing((String path) -> !path.startsWith("materials/")).thenComparing(java.util.Comparator.naturalOrder()));
        for (final var path : paths) {
            final var contents = resource(path);
            if (contents != null && ReferenceScanUtil.to(contents.toJson(), GroupType.Texture, name).size() > 0) {
                overridden.add(path);
                show(path, contents.get());
            }
        }

        announce();
    }

    /** Shows the contents of a file of a resource to the engine */
    private void show(final String path, final JsonObject value) {
        final var material = ResourceKindUtil.materialOf(path);
        if (material.isPresent())
            applier.applyMaterial(path, value, document, dependentsOf(material.get()));
        else
            applier.applyResource(path, value, document);
    }

    /**
     * The files of the things (objects, terrains) that name a material, as the things of the scene name them: the files that the scene
     * doesn't use are in the list too, they may have been loaded before.
     *
     * @param material the file of the material as the files name it, like {@code chest.json}
     */
    private List<ResourceKindUtil.Target> dependentsOf(final String material) {
        final var targets = new ArrayList<ResourceKindUtil.Target>();

        for (final var kind : List.of(OutlineKind.Object, OutlineKind.Terrain)) {
            final var folder = PlacementUtil.groupOf(kind).getArchiveFolder();
            for (final var file : filesOf(folder, List.of(".json"))) {
                final var contents = readResource(folder + "/" + file);
                if (contents != null && names(contents, material))
                    targets.add(new ResourceKindUtil.Target(kind, file, ResourceKindUtil.typeOf(kind)));
            }
        }

        return targets;
    }

    /**
     * Whether a field called {@code material} anywhere in the file has the value.
     */
    private static boolean names(final JsonElement element, final String material) {
        if (element.isJsonObject())
            return element.getAsJsonObject().entrySet().stream().anyMatch(entry -> "material".equals(entry.getKey()) && entry.getValue().isJsonPrimitive()
                    && material.equals(entry.getValue().getAsString()) || names(entry.getValue(), material));

        return element.isJsonArray() && element.getAsJsonArray().asList().stream().anyMatch(child -> names(child, material));
    }

    private ResourceDocument resource(final String path) {
        final var known = resources.get(path);
        if (known != null || session.getProject() == null)
            return known;

        try {
            final var resource = new ResourceDocument(path, Files.readString(session.getProject().resolve(path), StandardCharsets.UTF_8));
            resources.put(path, resource);
            return resource;
        } catch (final IOException | RuntimeException exception) {
            log.error("Can't read the resource {}", path, exception);
            EventUtil.post(new StatusEvent("Can't read %s: %s", path, exception.getMessage()));
            return null;
        }
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
            for (final var resource : resources.values())
                if (resource.isModified()) {
                    writeFile(session.getProject().resolve(resource.getPath()), resource.toJson());
                    resource.markSaved();

                    // What is made of the file the next time is made of what was written
                    forget(resource.getPath());
                }

            writeFile(file, document.toJson());
        } catch (final IOException exception) {
            log.error("Can't save the scene {}", file, exception);
            EventUtil.post(new StatusEvent("Can't save the scene %s: %s", document.getName(), exception.getMessage()));
            return false;
        }

        // The scene is made from its file the next time it is open
        context.getSceneRepository().forget(document.getName());

        EventUtil.post(new FilesWrittenEvent());
        history.markSaved();
        announceHistory();
        EventUtil.post(new StatusEvent("Scene %s is saved", document.getName()));
        return true;
    }

    /** Writes a file in one step: a file that can't be finished is not left half written */
    private void writeFile(final Path file, final String text) throws IOException {
        final var temporary = file.resolveSibling(file.getFileName() + ".saving");
        Files.writeString(temporary, text, StandardCharsets.UTF_8);
        Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private void forget(final String path) {
        if (path.startsWith("lights/"))
            context.getLightRepository().forget(path.substring("lights/".length()));
    }

    /** What the commands write with: the file is changed, and the scene of the engine and the panels show it */
    @Override
    public void write(final OutlineKind kind, final int index, final JsonObject entry) {
        document.setEntry(kind, index, entry);
        applier.apply(kind, index, entry);

        announce();
    }

    @Override
    public void write(final String section, final JsonObject value) {
        final var previous = document.getSection(section);
        document.setSection(section, value);

        try {
            applier.applySection(section, document.getScene());
        } catch (final RuntimeException exception) {
            // The engine can't show it (a file is missing, a number is out of range): the file is as it was
            document.setSection(section, previous);
            throw exception;
        }

        announce();
    }

    @Override
    public void writeResource(final String path, final JsonObject value) {
        final var resource = resource(path);
        final var previous = resource.get();
        resource.set(value);

        try {
            overridden.add(path);
            show(path, value);
        } catch (final RuntimeException exception) {
            // The engine can't show it (a file is missing, a number is out of range): the file is as it was, and so is what is made of it
            resource.set(previous);
            try {
                show(path, previous);
            } catch (final RuntimeException again) {
                log.error("Can't show {} as it was", path, again);
            }

            throw exception;
        }

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

            // The fields that showed what the user typed show the scene again
            announce();
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
