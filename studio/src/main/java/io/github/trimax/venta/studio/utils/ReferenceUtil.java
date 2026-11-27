package io.github.trimax.venta.studio.utils;

import java.util.List;
import java.util.Map;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * The fields of the files of resources that name another file of the project: where such a file is, and what its name ends with, so the
 * editor offers the files of the project to choose from.
 */
@UtilityClass
public final class ReferenceUtil {
    /**
     * Where the files that a field names are.
     *
     * @param folder     the folder of the project
     * @param extensions how the names of the files end
     */
    public record Place(String folder, List<String> extensions) {
    }

    private static final Map<String, Place> PLACES = Map.ofEntries(
            Map.entry("program", new Place("programs", List.of(".json"))),
            Map.entry("sprite", new Place("sprites", List.of(".json"))),
            Map.entry("material", new Place("materials", List.of(".json"))),
            Map.entry("mesh", new Place("meshes", List.of(".obj"))),
            Map.entry("sound", new Place("sounds", List.of(".ogg"))),
            Map.entry("texture", new Place("textures", List.of(".png", ".jpg", ".jpeg"))),
            Map.entry("gridMesh", new Place("gridmeshes", List.of(".json"))),
            Map.entry("heightmap", new Place("textures", List.of(".png", ".jpg", ".jpeg"))),

            // The textures of a material are named by what they are for
            Map.entry("ambientOcclusion", new Place("textures", List.of(".png", ".jpg", ".jpeg"))),
            Map.entry("diffuse", new Place("textures", List.of(".png", ".jpg", ".jpeg"))),
            Map.entry("emissive", new Place("textures", List.of(".png", ".jpg", ".jpeg"))),
            Map.entry("height", new Place("textures", List.of(".png", ".jpg", ".jpeg"))),
            Map.entry("metalness", new Place("textures", List.of(".png", ".jpg", ".jpeg"))),
            Map.entry("normal", new Place("textures", List.of(".png", ".jpg", ".jpeg"))),
            Map.entry("roughness", new Place("textures", List.of(".png", ".jpg", ".jpeg"))));

    /** Where the files are that a field with the name names, null for a field that doesn't name a file */
    public Place placeOf(@NonNull final String field) {
        return PLACES.get(field);
    }
}
