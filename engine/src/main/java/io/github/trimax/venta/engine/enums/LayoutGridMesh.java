package io.github.trimax.venta.engine.enums;

import io.github.trimax.venta.engine.utils.EnumUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum LayoutGridMesh {
    Position(0, 3),
    TextureCoordinates(1, 2);

    private final int locationID;
    private final int size;

    public static int getStride() {
        return Float.BYTES * getFloatsCount();
    }

    public static int getFloatsCount() {
        return EnumUtil.sum(LayoutGridMesh.class, LayoutGridMesh::getSize);
    }
}
