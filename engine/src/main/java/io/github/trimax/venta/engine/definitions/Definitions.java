package io.github.trimax.venta.engine.definitions;

import io.github.trimax.venta.engine.model.common.dto.Frame;
import lombok.experimental.UtilityClass;
import org.joml.Vector4f;

import java.util.List;

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

    /* Text scale */
    public static final float CONSOLE_TEXT_SCALE = 0.48f;

    /* Line height scale */
    public static final float CONSOLE_LINE_HEIGHT_SCALE = 0.8f;

    /* The height of the line (px) */
    public static final int CONSOLE_LINE_HEIGHT = 24;

    /* Console margins */
    public static final int CONSOLE_MARGIN_LEFT = 16;
    public static final int CONSOLE_MARGIN_BOTTOM = 32;

    /* Command line prefix */
    public static final String CONSOLE_WELCOME_SYMBOL = "> ";

    /*** Sound ***/
    public static final int SOUND_FREQUENCY = 44100;

    /*** Colors ***/
    public static final Vector4f COLOR_WHITE = new Vector4f(1.f, 1.f, 1.f, 1.f);

    /** Frames **/
    public static final List<Frame> DEFAULT_FRAMES = List.of(
            new Frame(0, 0, 1, 1)
    );
}
