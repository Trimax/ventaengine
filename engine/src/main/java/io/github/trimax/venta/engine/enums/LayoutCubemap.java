package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import one.util.streamex.StreamEx;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum LayoutCubemap {
    Position(0, 3);

    private final int locationID;
    private final int size;

    public static int getStride() {
        return Float.BYTES * getFloatsCount();
    }

    public static int getFloatsCount() {
        return StreamEx.of(values()).mapToInt(LayoutCubemap::getSize).sum();
    }
}
