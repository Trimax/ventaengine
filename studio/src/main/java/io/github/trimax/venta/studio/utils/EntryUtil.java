package io.github.trimax.venta.studio.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Reads and writes the fields of a thing of a scene file (a JSON object): its name, its place, its angles and its size. A number is
 * written with at most four decimals, so a value that was computed (a drag) doesn't leave digits of the float behind it.
 */
@UtilityClass
public final class EntryUtil {
    public static final String NAME = "name";
    public static final String POSITION = "position";
    public static final String ANGLES = "angles";
    public static final String SCALE = "scale";
    public static final String DIRECTION = "direction";
    public static final String SIZE = "size";

    private static final float PRECISION = 10000.f;

    public String getName(@NonNull final JsonObject entry) {
        return entry.has(NAME) ? entry.get(NAME).getAsString() : "";
    }

    public void setName(@NonNull final JsonObject entry, @NonNull final String name) {
        entry.addProperty(NAME, name);
    }

    /**
     * The vector of the field, the fallback when the thing has none (an optional field of the file).
     */
    public Vector3f getVector(@NonNull final JsonObject entry, @NonNull final String field, @NonNull final Vector3fc fallback) {
        if (!entry.has(field) || !entry.get(field).isJsonObject())
            return new Vector3f(fallback);

        final var vector = entry.getAsJsonObject(field);
        return new Vector3f(number(vector, "x", fallback.x()), number(vector, "y", fallback.y()), number(vector, "z", fallback.z()));
    }

    public Vector2f getVector(@NonNull final JsonObject entry, @NonNull final String field, @NonNull final Vector2fc fallback) {
        if (!entry.has(field) || !entry.get(field).isJsonObject())
            return new Vector2f(fallback);

        final var vector = entry.getAsJsonObject(field);
        return new Vector2f(number(vector, "x", fallback.x()), number(vector, "y", fallback.y()));
    }

    public void setVector(@NonNull final JsonObject entry, @NonNull final String field, @NonNull final Vector3fc value) {
        final var vector = new JsonObject();
        vector.add("x", round(value.x()));
        vector.add("y", round(value.y()));
        vector.add("z", round(value.z()));

        entry.add(field, vector);
    }

    public void setVector(@NonNull final JsonObject entry, @NonNull final String field, @NonNull final Vector2fc value) {
        final var vector = new JsonObject();
        vector.add("x", round(value.x()));
        vector.add("y", round(value.y()));

        entry.add(field, vector);
    }

    /** Whether the thing has the field in the file */
    public boolean has(@NonNull final JsonObject entry, @NonNull final String field) {
        return entry.has(field) && entry.get(field).isJsonObject();
    }

    private float number(final JsonObject vector, final String field, final float fallback) {
        return vector.has(field) ? vector.get(field).getAsFloat() : fallback;
    }

    private JsonPrimitive round(final float value) {
        return new JsonPrimitive(Math.round(value * PRECISION) / PRECISION);
    }
}
