package io.github.trimax.venta.studio.utils;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import org.joml.Vector3fc;

import com.google.gson.JsonObject;

import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.studio.model.outline.OutlineKind;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Putting a file of the project in a scene: which files are things that a scene holds, and what the scene file says about a new thing
 * (the engine needs some of the fields: an object has its angles and scale in the file).
 */
@UtilityClass
public final class PlacementUtil {
    /** The groups of the files that can be put in a scene, and what they become */
    private static final Map<GroupType, OutlineKind> KINDS = Map.of(
            GroupType.Object, OutlineKind.Object,
            GroupType.Light, OutlineKind.Light,
            GroupType.Emitter, OutlineKind.Emitter,
            GroupType.Billboard, OutlineKind.Billboard,
            GroupType.AudioSource, OutlineKind.Sound,
            GroupType.TerrainSurface, OutlineKind.Terrain,
            GroupType.WaterSurface, OutlineKind.Water);

    /** The field of the file of the scene that names the file of the thing, by the kind */
    private static final Map<OutlineKind, String> FIELDS = Map.of(
            OutlineKind.Object, "object",
            OutlineKind.Light, "light",
            OutlineKind.Emitter, "emitter",
            OutlineKind.Billboard, "billboard",
            OutlineKind.Sound, "soundSource",
            OutlineKind.Terrain, "terrainSurface",
            OutlineKind.Water, "waterSurface");

    /** The field of the file of the scene that names the file of the thing of the kind, null for a kind that is not a thing */
    public String fileFieldOf(final OutlineKind kind) {
        return FIELDS.get(kind);
    }

    /** What a file of the group becomes in a scene, empty for a file that a scene can't hold (a texture, a material, a scene) */
    public Optional<OutlineKind> kindOf(final GroupType group) {
        return Optional.ofNullable(group).map(KINDS::get);
    }

    /** What a file of the project becomes in a scene, empty for a file that isn't a thing of a scene */
    public Optional<OutlineKind> kindOf(@NonNull final Path project, @NonNull final Path file) {
        if (!file.getFileName().toString().endsWith(".json"))
            return Optional.empty();

        return kindOf(ProjectUtil.groupOf(project, file));
    }

    /** The group of the files that become things of the kind */
    public GroupType groupOf(@NonNull final OutlineKind kind) {
        return KINDS.entrySet().stream().filter(entry -> entry.getValue() == kind).map(Map.Entry::getKey).findFirst().orElseThrow();
    }

    /**
     * How the scene file names the file: the path inside the folder of its group, {@code trees/pine.json}.
     */
    public String resourceOf(@NonNull final Path project, @NonNull final Path file) {
        final var group = ProjectUtil.groupOf(project, file);
        if (group == null)
            throw new IllegalArgumentException(file + " is not in a folder of the group of resources");

        return project.resolve(group.getArchiveFolder()).relativize(file).toString().replace('\\', '/');
    }

    /**
     * What the scene file says about a new thing: the name, the file it is made of, the place, and what the engine wants to be there.
     */
    public JsonObject entryFor(@NonNull final OutlineKind kind, @NonNull final String resource, @NonNull final String name, @NonNull final Vector3fc position) {
        final var field = FIELDS.get(kind);
        if (field == null)
            throw new IllegalArgumentException(kind + " is not a thing of a scene");

        final var entry = new JsonObject();
        EntryUtil.setName(entry, name);
        entry.addProperty(field, resource);
        EntryUtil.setVector(entry, EntryUtil.POSITION, position);

        // The file of an object has its angles and scale always, the file of a billboard has its size
        if (kind == OutlineKind.Object) {
            EntryUtil.setVector(entry, EntryUtil.ANGLES, new org.joml.Vector3f());
            EntryUtil.setVector(entry, EntryUtil.SCALE, new org.joml.Vector3f(1.f));
        }

        if (kind == OutlineKind.Billboard)
            EntryUtil.setVector(entry, EntryUtil.SIZE, new org.joml.Vector2f(1.f));

        return entry;
    }
}
