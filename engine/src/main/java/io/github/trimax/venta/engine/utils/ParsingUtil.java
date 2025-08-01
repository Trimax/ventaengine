package io.github.trimax.venta.engine.utils;

import org.joml.Vector2f;
import org.joml.Vector3f;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ParsingUtil {
    public boolean asBoolean(final String value) {
        return Boolean.parseBoolean(value);
    }

    public int asInteger(final String value) {
        return Integer.parseInt(value);
    }

    public float asFloat(final String value) {
        return Float.parseFloat(value);
    }

    public Vector2f asVector2f(final String value) {
        final String[] components = asVector(value, "[x y]").split("\\s+");
        if (components.length != 2)
            throw new IllegalArgumentException("Expected 2 components for Vector2f, got: " + value);

        return new Vector2f(Float.parseFloat(components[0]), Float.parseFloat(components[1]));
    }

    public Vector3f asVector3f(final String value) {
        final String[] components = asVector(value, "[x y z]").split("\\s+");
        if (components.length != 3)
            throw new IllegalArgumentException("Expected 3 components for Vector3f, got: " + value);

        return new Vector3f(Float.parseFloat(components[0]), Float.parseFloat(components[1]), Float.parseFloat(components[2]));
    }

    private String asVector(final String value, final String expectedFormat) {
        final var trimmedValue = value.trim();
        if (trimmedValue.startsWith("[") && trimmedValue.endsWith("]"))
            return trimmedValue.substring(1, trimmedValue.length() - 1).trim();

        throw new IllegalArgumentException("Expected string in format " + expectedFormat + ", got: " + value);
    }
}
