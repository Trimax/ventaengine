package io.github.trimax.venta.engine.enums;

public enum MeshFormat {
    JSON,
    OBJ;

    public static MeshFormat of(final String extension) {
        for (final var currentValue : values())
            if (currentValue.name().equalsIgnoreCase(extension))
                return currentValue;

        return null;
    }
}
