package com.venta.engine.definitions;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Definitions {
    public static final int LIGHT_MAX = 64;

    /*** Basic ***/

    public static final int COUNT_VERTICES_PER_FACET = 3;
    public static final int COUNT_VERTICES_PER_EDGE = 2;

    /*** Vertex buffer ***/

    /* The number of float values per vertex */
    public static final int VERTEX_FLOATS_COUNT = 18;

    /* Position components offset */
    public static final int VERTEX_OFFSET_POSITION_X = 0;
    public static final int VERTEX_OFFSET_POSITION_Y = 1;
    public static final int VERTEX_OFFSET_POSITION_Z = 2;

    /* Normal components offset */
    public static final int VERTEX_OFFSET_NORMAL_X = 3;
    public static final int VERTEX_OFFSET_NORMAL_Y = 4;
    public static final int VERTEX_OFFSET_NORMAL_Z = 5;

    /* Texture coordinates components offset */
    public static final int VERTEX_OFFSET_TEXTURE_COORDINATES_U = 6;
    public static final int VERTEX_OFFSET_TEXTURE_COORDINATES_V = 7;

    /* Color components offset */
    public static final int VERTEX_OFFSET_COLOR_R = 8;
    public static final int VERTEX_OFFSET_COLOR_G = 9;
    public static final int VERTEX_OFFSET_COLOR_B = 10;
    public static final int VERTEX_OFFSET_COLOR_A = 11;

    /* Tangent components offset */
    public static final int VERTEX_OFFSET_TANGENT_X = 12;
    public static final int VERTEX_OFFSET_TANGENT_Y = 13;
    public static final int VERTEX_OFFSET_TANGENT_Z = 14;

    /* Bitangent components offset */
    public static final int VERTEX_OFFSET_BITANGENT_X = 15;
    public static final int VERTEX_OFFSET_BITANGENT_Y = 16;
    public static final int VERTEX_OFFSET_BITANGENT_Z = 17;

    /*** Facets buffer ***/

    /* Position components offset */
    public static final int FACET_OFFSET_VERTEX_1 = 0;
    public static final int FACET_OFFSET_VERTEX_2 = 1;
    public static final int FACET_OFFSET_VERTEX_3 = 2;

    /*** Edges buffer ***/

    /* Position components offset */
    public static final int EDGE_OFFSET_VERTEX_1 = 0;
    public static final int EDGE_OFFSET_VERTEX_2 = 1;
}
