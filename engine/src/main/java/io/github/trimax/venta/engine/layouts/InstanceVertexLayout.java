package io.github.trimax.venta.engine.layouts;

import io.github.trimax.venta.engine.utils.EnumUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The attributes of one copy of a mesh drawn with instancing. They follow the attributes of the vertices
 * ({@link MeshVertexLayout}) and are stored one after another in the buffer of the copies.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum InstanceVertexLayout implements AbstractVertexLayout {
    /** The model matrix: a `mat4` takes four locations of four floats */
    MatrixModel(6, 16, 4),

    /** The normal matrix: a `mat3` takes three locations of three floats */
    MatrixNormal(10, 9, 3),

    /**
     * How far the copy fades in or out while a level of detail replaces another: 1 is a copy that is drawn whole, 0 to 1 is
     * a level that fades in (the dither lets this share of the pixels through), 0 to -1 is a level that fades out (the share
     * of the pixels is the absolute value, the dither is the opposite of the one that fades in)
     */
    Fade(13, 1, 1);

    private final int locationID;
    private final int size;
    private final int columns;

    public static int getFloatsCount() {
        return EnumUtil.sum(InstanceVertexLayout.class, InstanceVertexLayout::getSize);
    }

    public static int getStride() {
        return Float.BYTES * getFloatsCount();
    }
}
