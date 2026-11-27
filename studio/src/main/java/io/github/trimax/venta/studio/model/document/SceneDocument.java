package io.github.trimax.venta.studio.model.document;

import java.util.List;
import java.util.Map;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.trimax.venta.engine.model.dto.SceneDTO;
import io.github.trimax.venta.engine.utils.ParsingUtil;
import io.github.trimax.venta.studio.model.outline.OutlineKind;
import lombok.Getter;
import lombok.NonNull;

/**
 * The file of a scene as the editor holds it: the tree of the JSON, which is changed and written back, so the order of the fields, the
 * fields that the engine doesn't know and everything that was not touched are saved as they were. The scene that the panels and the engine
 * show ({@link #getScene()}) is read from the tree after every change.
 */
public final class SceneDocument {
    /** The arrays of the file that hold the things of the scene, by their kinds */
    private static final Map<OutlineKind, String> ARRAYS = Map.of(
            OutlineKind.Object, "objects",
            OutlineKind.Light, "lights",
            OutlineKind.Emitter, "emitters",
            OutlineKind.Billboard, "billboards",
            OutlineKind.Sound, "soundSources",
            OutlineKind.Terrain, "terrainSurfaces",
            OutlineKind.Water, "waterSurfaces");

    /**
     * The sections of the environment that can be edited, by the names of the hierarchy: the fields of the file that hold them. A section
     * is the object of its field, or (the output) the fields themselves.
     */
    private static final Map<String, List<String>> SECTIONS = Map.ofEntries(
            Map.entry("Sun", List.of("directionalLight")),
            Map.entry("Ambient light", List.of("ambientLight")),
            Map.entry("Fog", List.of("fog")),
            Map.entry("Wind", List.of("wind")),
            Map.entry("Bloom", List.of("bloom")),
            Map.entry("Light shafts", List.of("lightShafts")),
            Map.entry("Ambient occlusion", List.of("ambientOcclusion")),
            Map.entry("Antialiasing", List.of("antialiasing")),
            Map.entry("View", List.of("view")),
            Map.entry("Output", List.of("exposure", "toneMapping")));

    /** The sections that are the fields of the file themselves, not objects */
    private static final String OUTPUT = "Output";

    private static final com.google.gson.Gson WRITER = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @Getter
    private final String name;

    private final JsonObject root;

    @Getter
    private SceneDTO scene;

    /**
     * A scene read from the text of its file.
     *
     * @param name the name of the file, like {@code beach.json}
     * @param json the contents of the file
     */
    public SceneDocument(@NonNull final String name, @NonNull final String json) {
        this.name = name;

        final var parsed = JsonParser.parseString(json);
        if (!parsed.isJsonObject())
            throw new IllegalArgumentException("A scene file is an object, " + name + " is not");

        this.root = parsed.getAsJsonObject();
        read();
    }

    /** Whether the kind has things in the file: the settings and the categories have none */
    public static boolean isThingKind(final OutlineKind kind) {
        return ARRAYS.containsKey(kind);
    }

    /** How many things of the kind the file has */
    public int count(@NonNull final OutlineKind kind) {
        final var array = array(kind, false);
        return array == null ? 0 : array.size();
    }

    /** A copy of the thing that has the place in the file: the copy can be changed without changing the document */
    public JsonObject getEntry(@NonNull final OutlineKind kind, final int index) {
        return array(kind, true).get(checked(kind, index)).getAsJsonObject().deepCopy();
    }

    /** Puts the thing in the place of the one that is there */
    public void setEntry(@NonNull final OutlineKind kind, final int index, @NonNull final JsonObject entry) {
        array(kind, true).set(checked(kind, index), entry.deepCopy());
        read();
    }

    /** Puts a thing in the place: the things after it move one place on */
    public void insertEntry(@NonNull final OutlineKind kind, final int index, @NonNull final JsonObject entry) {
        final var array = array(kind, true);
        if (index < 0 || index > array.size())
            throw new IndexOutOfBoundsException(String.format("%s %d of %d", kind, index, array.size()));

        // A JSON array has no insertion: the tail is taken out and put back after the new thing
        final var tail = new JsonArray();
        while (array.size() > index)
            tail.add(array.remove(index));

        array.add(entry.deepCopy());
        tail.forEach(array::add);
        read();
    }

    /** Takes the thing out of the file: the things after it move one place back */
    public void removeEntry(@NonNull final OutlineKind kind, final int index) {
        array(kind, true).remove(checked(kind, index));
        read();
    }

    /** Whether the name is a section of the environment that can be edited and the file has it */
    public boolean hasSection(@NonNull final String name) {
        final var keys = SECTIONS.get(name);
        return keys != null && keys.stream().anyMatch(root::has);
    }

    /** A copy of a section of the environment: the object of its field, or the fields of the output together */
    public JsonObject getSection(@NonNull final String name) {
        final var keys = sectionKeys(name);
        if (!OUTPUT.equals(name))
            return root.get(keys.getFirst()).getAsJsonObject().deepCopy();

        final var section = new JsonObject();
        keys.stream().filter(root::has).forEach(key -> section.add(key, root.get(key).deepCopy()));
        return section;
    }

    /** Puts a section in the file in place of the one that is there; the fields that the section doesn't have are taken out (the output only) */
    public void setSection(@NonNull final String name, @NonNull final JsonObject section) {
        final var keys = sectionKeys(name);
        if (OUTPUT.equals(name))
            keys.forEach(key -> {
                if (section.has(key))
                    root.add(key, section.get(key).deepCopy());
                else
                    root.remove(key);
            });
        else
            root.add(keys.getFirst(), section.deepCopy());

        read();
    }

    /** The fields of the file that a section is made of, empty for a name that is not a section */
    public static List<String> fieldsOf(@NonNull final String name) {
        return SECTIONS.getOrDefault(name, List.of());
    }

    private List<String> sectionKeys(final String name) {
        if (!hasSection(name))
            throw new IllegalArgumentException(name + " is not a section of the scene " + this.name);

        return SECTIONS.get(name);
    }

    /** The text of the file as it is written: indented by two spaces, the order of the fields is kept */
    public String toJson() {
        return WRITER.toJson(root) + "\n";
    }

    private void read() {
        scene = ParsingUtil.asObject(root.toString(), SceneDTO.class);
    }

    private int checked(final OutlineKind kind, final int index) {
        final var count = count(kind);
        if (index < 0 || index >= count)
            throw new IndexOutOfBoundsException(String.format("%s %d of %d", kind, index, count));

        return index;
    }

    private JsonArray array(final OutlineKind kind, final boolean create) {
        final var key = ARRAYS.get(kind);
        if (key == null)
            throw new IllegalArgumentException(kind + " is not a thing of a scene");

        final JsonElement found = root.get(key);
        if (found != null && found.isJsonArray())
            return found.getAsJsonArray();

        if (!create)
            return null;

        final var array = new JsonArray();
        root.add(key, array);
        return array;
    }
}
