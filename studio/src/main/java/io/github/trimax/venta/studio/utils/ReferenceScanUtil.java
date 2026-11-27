package io.github.trimax.venta.studio.utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import io.github.trimax.venta.engine.enums.GroupType;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Finds the places in a JSON text where a file of the project is named, without changing the text around them: a name belongs to a
 * field ({@code "material": "chest.json"}), and the field says which group of resources the file is in, so a material and an object that
 * have the same name are told apart. Every other character of the text stays as it is, so a file that is changed has no other changes.
 */
@UtilityClass
public final class ReferenceScanUtil {
    /**
     * A string of a JSON text that is a value.
     *
     * @param start the place in the text of the first character of the string (after the quote)
     * @param end   the place after the last character of the string (before the quote)
     * @param key   the field the string is the value of, for the elements of a list the field of the list; null for a string with none
     * @param value the characters of the string as they are written
     */
    public record Reference(int start, int end, String key, String value) {
    }

    private static final Map<String, GroupType> FIELDS = Map.ofEntries(
            // The things of a scene
            Map.entry("object", GroupType.Object),
            Map.entry("light", GroupType.Light),
            Map.entry("emitter", GroupType.Emitter),
            Map.entry("billboard", GroupType.Billboard),
            Map.entry("soundSource", GroupType.AudioSource),
            Map.entry("terrainSurface", GroupType.TerrainSurface),
            Map.entry("waterSurface", GroupType.WaterSurface),
            Map.entry("skybox", GroupType.CubeMap),

            // What the files of the things name
            Map.entry("program", GroupType.Program),
            Map.entry("shaderVertex", GroupType.Shader),
            Map.entry("shaderFragment", GroupType.Shader),
            Map.entry("sprite", GroupType.Sprite),
            Map.entry("material", GroupType.Material),
            Map.entry("mesh", GroupType.Mesh),
            Map.entry("sound", GroupType.Sound),
            Map.entry("gridMesh", GroupType.GridMesh),
            Map.entry("textures", GroupType.Texture),
            Map.entry("texture", GroupType.Texture),
            Map.entry("heightmap", GroupType.Texture),
            Map.entry("equirectangular", GroupType.Texture));

    /** The fields that name a texture by what it is for (the textures of a material) or by the face of a cube map */
    private static final List<String> TEXTURE_FIELDS = List.of("ambientOcclusion", "diffuse", "emissive", "height", "metalness", "normal", "roughness",
            "Right", "Left", "Front", "Back", "Top", "Bottom");

    /** The group of the files that a field names, null for a field that doesn't name a file */
    public GroupType groupOf(final String field) {
        if (field == null)
            return null;

        return TEXTURE_FIELDS.contains(field) ? GroupType.Texture : FIELDS.get(field);
    }

    /** Every string of the text that is a value (not a name of a field), with the field it belongs to */
    public List<Reference> references(@NonNull final String json) {
        final var found = new ArrayList<Reference>();
        final var containers = new ArrayDeque<Container>();

        var index = 0;
        while (index < json.length()) {
            final var symbol = json.charAt(index);

            if (symbol == '"') {
                final var end = endOfString(json, index + 1);
                if (end < 0)
                    break;

                final var text = json.substring(index + 1, end);
                if (isName(json, end + 1)) {
                    if (!containers.isEmpty())
                        containers.peek().field = text;
                } else {
                    found.add(new Reference(index + 1, end, ownerOf(containers), text));
                }

                index = end + 1;
                continue;
            }

            if (symbol == '{' || symbol == '[')
                containers.push(new Container(symbol == '[', ownerOf(containers)));
            else if ((symbol == '}' || symbol == ']') && !containers.isEmpty())
                containers.pop();

            index++;
        }

        return found;
    }

    /**
     * The places of the text that name a file.
     *
     * @param group the group of resources the file is in
     * @param name  the name of the file as the files name it: the path from the folder of the group
     */
    public List<Reference> to(@NonNull final String json, @NonNull final GroupType group, @NonNull final String name) {
        return references(json).stream().filter(reference -> groupOf(reference.key()) == group && reference.value().equals(name)).toList();
    }

    /**
     * The places of the text that name a file inside of a folder.
     *
     * @param group  the group of resources the folder is in
     * @param prefix the name of the folder as the files name it, with the slash at the end: {@code chest/}
     */
    public List<Reference> inside(@NonNull final String json, @NonNull final GroupType group, @NonNull final String prefix) {
        return references(json).stream().filter(reference -> groupOf(reference.key()) == group && reference.value().startsWith(prefix)).toList();
    }

    /** The text with every place named by a reference given the content that the function makes of what it had; the rest is as it was */
    public String replaceEach(@NonNull final String json, @NonNull final List<Reference> places, @NonNull final java.util.function.UnaryOperator<String> content) {
        final var text = new StringBuilder(json);
        places.stream().sorted((first, second) -> Integer.compare(second.start(), first.start())).forEach(place -> text.replace(place.start(), place.end(), content.apply(place.value())));

        return text.toString();
    }

    /** The text with the places named by the references given another content; the rest of the text is as it was */
    public String replace(@NonNull final String json, @NonNull final List<Reference> places, @NonNull final String content) {
        final var text = new StringBuilder(json);
        places.stream().sorted((first, second) -> Integer.compare(second.start(), first.start())).forEach(place -> text.replace(place.start(), place.end(), content));

        return text.toString();
    }

    /** The field that a value in the container belongs to: the last name of an object, the field of the list for a list */
    private String ownerOf(final Deque<Container> containers) {
        if (containers.isEmpty())
            return null;

        final var container = containers.peek();
        return container.list ? container.owner : container.field;
    }

    /** The place of the quote that ends a string that starts at the place, -1 when the text ends first */
    private int endOfString(final String json, final int start) {
        for (int index = start; index < json.length(); index++) {
            if (json.charAt(index) == '\\')
                index++;
            else if (json.charAt(index) == '"')
                return index;
        }

        return -1;
    }

    /** Whether a colon follows (after the spaces): the string is a name of a field, not a value */
    private boolean isName(final String json, final int from) {
        for (int index = from; index < json.length(); index++) {
            if (!Character.isWhitespace(json.charAt(index)))
                return json.charAt(index) == ':';
        }

        return false;
    }

    /** An object or a list that the scan is inside of */
    private static final class Container {
        private final boolean list;

        /** What the container is the value of, for a list */
        private final String owner;

        /** The last name of a field read, for an object */
        private String field;

        private Container(final boolean list, final String owner) {
            this.list = list;
            this.owner = owner;
        }
    }
}
