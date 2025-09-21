package io.github.trimax.venta.engine.layouts;

import io.github.trimax.venta.engine.utils.EnumUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter(onMethod_ = @__(@Override))
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MeshVertexLayout implements AbstractVertexLayout {
    Position(0, 3),
    Normal(1, 3),
    Tangent(2, 3),
    Bitangent(3, 3),
    TextureCoordinates(4, 2),
    Color(5, 4);

    private final int locationID;
    private final int size;

    public static int getFloatsCount() {
        return EnumUtil.sum(MeshVertexLayout.class, MeshVertexLayout::getSize);
    }
}
