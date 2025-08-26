package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum CubemapFace {
    Right(0),
    Left(1),
    Top(2),
    Bottom(3),
    Front(4),
    Back(5);

    private final int index;
}