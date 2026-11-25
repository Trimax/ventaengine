package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * How far FXAA looks along an edge to find where it ends: the longer it looks, the better it smooths the long, almost
 * horizontal and almost vertical edges, and the more samples every pixel of an edge costs.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FxaaQuality {
    Low(8),
    Medium(16),
    High(32);

    /** The number of steps along an edge in each direction, at most */
    private final int steps;
}
