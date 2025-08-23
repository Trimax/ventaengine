package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum DataType {
    Materials,
    Textures,
    Programs,
    Shaders,
    Objects,
    Scenes,
    Lights,
    Meshes;

    public String shortenPath(@NonNull final String path) {
        return path.substring(String.format("/%s", name()).length() + 1);
    }

    public static DataType of(final String value) {
        for (final var currentValue : values())
            if (currentValue.name().equalsIgnoreCase(value))
                return currentValue;

        return null;
    }
}
