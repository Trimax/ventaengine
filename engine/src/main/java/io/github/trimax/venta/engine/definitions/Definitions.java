package io.github.trimax.venta.engine.definitions;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Definitions {
    public static final int LIGHT_MAX = 64;
    public static final int WAVE_MAX = 16;

    /*** Basic ***/

    public static final int COUNT_VERTICES_PER_FACET = 3;
    public static final int COUNT_VERTICES_PER_EDGE = 2;

    /*** Vertex buffer ***/

    /* Position components offset */
    public static final int VERTEX_OFFSET_POSITION_X = 0;
    public static final int VERTEX_OFFSET_POSITION_Y = 1;
    public static final int VERTEX_OFFSET_POSITION_Z = 2;

    /* Normal components offset */
    public static final int VERTEX_OFFSET_NORMAL_X = 3;
    public static final int VERTEX_OFFSET_NORMAL_Y = 4;
    public static final int VERTEX_OFFSET_NORMAL_Z = 5;

    /* Tangent components offset */
    public static final int VERTEX_OFFSET_TANGENT_X = 6;
    public static final int VERTEX_OFFSET_TANGENT_Y = 7;
    public static final int VERTEX_OFFSET_TANGENT_Z = 8;

    /* Bitangent components offset */
    public static final int VERTEX_OFFSET_BITANGENT_X = 9;
    public static final int VERTEX_OFFSET_BITANGENT_Y = 10;
    public static final int VERTEX_OFFSET_BITANGENT_Z = 11;

    /* Texture coordinates components offset */
    public static final int VERTEX_OFFSET_TEXTURE_COORDINATES_U = 12;
    public static final int VERTEX_OFFSET_TEXTURE_COORDINATES_V = 13;

    /* Color components offset */
    public static final int VERTEX_OFFSET_COLOR_R = 14;
    public static final int VERTEX_OFFSET_COLOR_G = 15;
    public static final int VERTEX_OFFSET_COLOR_B = 16;
    public static final int VERTEX_OFFSET_COLOR_A = 17;

    /*** Facets buffer ***/

    /* Position components offset */
    public static final int FACET_OFFSET_VERTEX_1 = 0;
    public static final int FACET_OFFSET_VERTEX_2 = 1;
    public static final int FACET_OFFSET_VERTEX_3 = 2;

    /*** Edges buffer ***/

    /* Position components offset */
    public static final int EDGE_OFFSET_VERTEX_1 = 0;
    public static final int EDGE_OFFSET_VERTEX_2 = 1;

    /*** Font atlas ***/

    /* The size of the font atlas */
    public static final int FONT_ATLAS_WIDTH = 2048;
    public static final int FONT_ATLAS_HEIGHT = 1024;

    /* The number of characters to bake into atlas */
    public static final int FONT_ATLAS_CHARACTERS_COUNT = 4096;

    /* Baked font height */
    public static final int FONT_HEIGHT = 32;

    /* The number of atlases */
    public static final int FONT_ATLAS_COUNT = (65536 + FONT_ATLAS_CHARACTERS_COUNT - 1) / FONT_ATLAS_CHARACTERS_COUNT;

    /*** Console ***/

    /* The vertical line interval */
    public static final float CONSOLE_LINE_INTERVAL = 1.2f;

    /* The height of the line (px) */
    public static final float CONSOLE_LINE_HEIGHT = 21f;

    /* Width of the character */
    public static final float CONSOLE_CHARACTER_WIDTH = 16f;

    /* Command line prefix */
    public static final String CONSOLE_WELCOME_SYMBOL = "> ";

    /* Horizontal scale factor */
    public static final float CONSOLE_SCALE_HORIZONTAL = 0.0015f * 800;

    /* Vertical scale factor */
    public static final float CONSOLE_SCALE_VERTICAL = 0.0015f * 600;

    /*** Sound ***/
    public static final int SOUND_FREQUENCY = 44100;
}
