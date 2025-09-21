package io.github.trimax.venta.engine.layouts;

import io.github.trimax.venta.engine.utils.EnumUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter(onMethod_ = @__(@Override))
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ConsoleVertexLayout implements AbstractVertexLayout {
    Position(0, 2);

    private final int locationID;
    private final int size;

    public static int getFloatsCount() {
        return EnumUtil.sum(ConsoleVertexLayout.class, ConsoleVertexLayout::getSize);
    }
}
