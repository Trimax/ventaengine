package io.github.trimax.venta.engine.layouts;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter(onMethod_ = @__(@Override))
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum GridMeshVertexLayout implements AbstractVertexLayout {
    Position(0, 3),
    TextureCoordinates(1, 2);

    private final int locationID;
    private final int size;
}
