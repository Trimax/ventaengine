package io.github.trimax.packer.enums;

import one.util.streamex.StreamEx;

public enum TextureChannel {
    Red,
    Green,
    Blue,
    Alpha;

    public static TextureChannel of(final String value) {
        return StreamEx.of(values())
                .filter(x -> x.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }
}
