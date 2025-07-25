package com.venta.engine.definitions;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Definitions {
    public static final int LIGHT_MAX = 64;

    /*** Basic ***/

    public static final int COUNT_FLOATS_PER_VERTEX = 18;
    public static final int COUNT_VERTICES_PER_FACET = 3;
    public static final int COUNT_VERTICES_PER_EDGE = 2;

    /*** Vertex buffer ***/

    /* Position components offset */
    public static final int VERTEX_ATTRIBUTE_INDEX_POSITION = 0;
    public static final int VERTEX_OFFSET_POSITION_X = 0;
    public static final int VERTEX_OFFSET_POSITION_Y = 1;
    public static final int VERTEX_OFFSET_POSITION_Z = 2;

    /* Normal components offset */
    public static final int VERTEX_ATTRIBUTE_INDEX_NORMAL = 1;
    public static final int VERTEX_OFFSET_NORMAL_X = 3;
    public static final int VERTEX_OFFSET_NORMAL_Y = 4;
    public static final int VERTEX_OFFSET_NORMAL_Z = 5;

    /* Tangent components offset */
    public static final int VERTEX_ATTRIBUTE_INDEX_TANGENT = 2;
    public static final int VERTEX_OFFSET_TANGENT_X = 6;
    public static final int VERTEX_OFFSET_TANGENT_Y = 7;
    public static final int VERTEX_OFFSET_TANGENT_Z = 8;

    /* Bitangent components offset */
    public static final int VERTEX_ATTRIBUTE_INDEX_BITANGENT = 3;
    public static final int VERTEX_OFFSET_BITANGENT_X = 9;
    public static final int VERTEX_OFFSET_BITANGENT_Y = 10;
    public static final int VERTEX_OFFSET_BITANGENT_Z = 11;

    /* Texture coordinates components offset */
    public static final int VERTEX_ATTRIBUTE_INDEX_TEXTURE_COORDINATES = 4;
    public static final int VERTEX_OFFSET_TEXTURE_COORDINATES_U = 12;
    public static final int VERTEX_OFFSET_TEXTURE_COORDINATES_V = 13;

    /* Color components offset */
    public static final int VERTEX_ATTRIBUTE_INDEX_COLOR = 5;
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
}
