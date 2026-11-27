package io.github.trimax.venta.studio.model.document;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.Getter;
import lombok.NonNull;

/**
 * The file of a resource of the project (a light, ...) as the editor holds it: the tree of the JSON, which is changed and written back, so
 * the fields that the editor doesn't know are saved as they were. It knows whether it differs from what was read or saved last.
 */
public final class ResourceDocument {
    private static final com.google.gson.Gson WRITER = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Where the file is in the project, like {@code lights/campfire.json}.
     */
    @Getter
    private final String path;

    private JsonObject root;
    private JsonObject saved;

    /**
     * A resource read from the text of its file.
     *
     * @param path the place of the file in the project
     * @param json the contents of the file
     */
    public ResourceDocument(@NonNull final String path, @NonNull final String json) {
        this.path = path;

        final var parsed = JsonParser.parseString(json);
        if (!parsed.isJsonObject())
            throw new IllegalArgumentException("A resource file is an object, " + path + " is not");

        this.root = parsed.getAsJsonObject();
        this.saved = root.deepCopy();
    }

    /** A copy of the contents: it can be changed without changing the document */
    public JsonObject get() {
        return root.deepCopy();
    }

    public void set(@NonNull final JsonObject value) {
        this.root = value.deepCopy();
    }

    /** Whether the contents differ from what was read or saved last */
    public boolean isModified() {
        return !root.equals(saved);
    }

    /** The file is written as it is now */
    public void markSaved() {
        saved = root.deepCopy();
    }

    /** The text of the file as it is written: indented by two spaces, the order of the fields is kept */
    public String toJson() {
        return WRITER.toJson(root) + "\n";
    }
}
