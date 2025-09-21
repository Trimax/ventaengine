package io.github.trimax.venta.engine.enums;

import io.github.trimax.venta.engine.utils.EnumUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
        return EnumUtil.sum(LayoutCubemap.class, LayoutCubemap::getSize);
    }
}
