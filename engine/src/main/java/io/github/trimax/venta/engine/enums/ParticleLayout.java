package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ParticleLayout {
    Position(0, 2),
    Color(1, 4),
    MatrixModel(2, 16);

    private final int locationID;
    private final int size;

    public int getStride() {
        return Float.BYTES * size;
    }
}
