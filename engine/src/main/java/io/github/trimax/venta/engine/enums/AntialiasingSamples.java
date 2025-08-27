package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AntialiasingSamples {
    X2(2),
    X4(4),
    X8(8),
    X16(16);

    private final int value;
}
