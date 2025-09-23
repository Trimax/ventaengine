package io.github.trimax.venta.engine.definitions;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class DefinitionsFont {
    /* The size of the font atlas */
    public static final int ATLAS_WIDTH = 2048;
    public static final int ATLAS_HEIGHT = 1024;

    /* The number of characters to bake into atlas */
    public static final int ATLAS_COUNT_CHARACTERS = 4096;

    /* The number of atlases */
    public static final int ATLAS_COUNT = (65536 + ATLAS_COUNT_CHARACTERS - 1) / ATLAS_COUNT_CHARACTERS;

    /* Baked font height */
    public static final int FONT_HEIGHT = 32;
}
