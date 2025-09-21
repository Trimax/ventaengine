package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum LayoutBillboard {
    Position(0, 2);

    private final int locationID;
    private final int size;

    public int getStride() {
        return Float.BYTES * size;
    }
}
