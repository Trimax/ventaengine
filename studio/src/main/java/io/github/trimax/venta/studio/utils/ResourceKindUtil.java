package io.github.trimax.venta.studio.utils;

import java.util.Map;
import java.util.Optional;

import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.engine.model.dto.BillboardDTO;
import io.github.trimax.venta.engine.model.dto.EmitterDTO;
import io.github.trimax.venta.engine.model.dto.LightDTO;
import io.github.trimax.venta.engine.model.dto.ObjectDTO;
import io.github.trimax.venta.engine.model.dto.SoundSourceDTO;
import io.github.trimax.venta.engine.model.dto.TerrainSurfaceDTO;
import io.github.trimax.venta.engine.model.dto.WaterSurfaceDTO;
import io.github.trimax.venta.studio.model.outline.OutlineKind;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Which things of a scene are made of a file of the project: the folder of the files of a kind, and the record that the engine reads
 * such a file into.
 */
@UtilityClass
public final class ResourceKindUtil {
    /**
     * A file of a resource that a thing of a scene is made of.
     *
     * @param kind the kind of the things
     * @param file the name of the file in the folder of the kind, as the scene names it: {@code campfire.json}
     * @param type the record that the engine reads the file into
     */
    public record Target(OutlineKind kind, String file, Class<?> type) {
    }

    private static final Map<OutlineKind, GroupType> GROUPS = Map.of(
            OutlineKind.Object, GroupType.Object,
            OutlineKind.Light, GroupType.Light,
            OutlineKind.Emitter, GroupType.Emitter,
            OutlineKind.Billboard, GroupType.Billboard,
            OutlineKind.Sound, GroupType.AudioSource,
            OutlineKind.Terrain, GroupType.TerrainSurface,
            OutlineKind.Water, GroupType.WaterSurface);

    private static final Map<OutlineKind, Class<?>> TYPES = Map.of(
            OutlineKind.Object, ObjectDTO.class,
            OutlineKind.Light, LightDTO.class,
            OutlineKind.Emitter, EmitterDTO.class,
            OutlineKind.Billboard, BillboardDTO.class,
            OutlineKind.Sound, SoundSourceDTO.class,
            OutlineKind.Terrain, TerrainSurfaceDTO.class,
            OutlineKind.Water, WaterSurfaceDTO.class);

    /** The path in the project of the file that the things of a kind are made of, empty for a kind that is not made of a file */
    public Optional<String> pathOf(@NonNull final OutlineKind kind, @NonNull final String file) {
        return Optional.ofNullable(GROUPS.get(kind)).filter(group -> !file.isEmpty()).map(group -> group.getArchiveFolder() + "/" + file);
    }

    /** What the file at a path of the project is for, empty when no thing of a scene is made of such a file */
    public Optional<Target> of(@NonNull final String path) {
        return GROUPS.entrySet().stream()
                .filter(entry -> path.startsWith(entry.getValue().getArchiveFolder() + "/"))
                .findFirst()
                .map(entry -> new Target(entry.getKey(), path.substring(entry.getValue().getArchiveFolder().length() + 1), TYPES.get(entry.getKey())));
    }

    /** The folder of the files of the materials */
    public static final String MATERIALS = "materials/";

    /**
     * The name of a material file as the files that use it name it ({@code lod/rock.json}), empty when the path is not of a material.
     */
    public Optional<String> materialOf(@NonNull final String path) {
        return path.startsWith(MATERIALS) && path.length() > MATERIALS.length() ? Optional.of(path.substring(MATERIALS.length())) : Optional.empty();
    }

    /** The record that the engine reads the files of the things of a kind into, null for a kind that is not made of a file */
    public Class<?> typeOf(@NonNull final OutlineKind kind) {
        return TYPES.get(kind);
    }
}
