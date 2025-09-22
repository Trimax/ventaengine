package io.github.trimax.venta.engine.layouts;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter(onMethod_ = @__(@Override))
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TextVertexLayout implements AbstractVertexLayout {
    Position(0, 2);

    private final int locationID;
    private final int size;
}
