package io.github.trimax.venta.engine.definitions;

import io.github.trimax.venta.engine.model.common.dto.Frame;
import lombok.experimental.UtilityClass;
import org.joml.Vector4f;

import java.util.List;

@UtilityClass
public final class DefinitionsBuffer {
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

    /*** Colors ***/
    public static final Vector4f COLOR_WHITE = new Vector4f(1.f, 1.f, 1.f, 1.f);

    /** Frames **/
    public static final List<Frame> DEFAULT_FRAMES = List.of(
            new Frame(0, 0, 1, 1)
    );
}
