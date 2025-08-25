package io.github.trimax.venta.engine.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public enum FogType {
    Linear(0),
    Exp(1);
    
    private final int value;
}
