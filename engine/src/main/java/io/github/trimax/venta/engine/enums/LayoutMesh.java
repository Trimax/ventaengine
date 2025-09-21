package io.github.trimax.venta.engine.enums;

import io.github.trimax.venta.engine.utils.EnumUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum LayoutMesh {
    Position(0, 3),
    Normal(1, 3),
    Tangent(2, 3),
    Bitangent(3, 3),
    TextureCoordinates(4, 2),
    Color(5, 4);

    private final int locationID;
    private final int size;

    public static int getStride() {
        return Float.BYTES * getFloatsCount();
    }

    public static int getFloatsCount() {
        return EnumUtil.sum(LayoutMesh.class, LayoutMesh::getSize);
    }
}
