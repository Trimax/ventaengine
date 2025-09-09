package io.github.trimax.venta.engine.memory;

import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;

import io.github.trimax.venta.container.annotations.Component;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MatrixCache {
    private final Map<Integer, Matrix4f> matrices = new HashMap<>();

    public Matrix4f get(final int depth) {
        return matrices.computeIfAbsent(depth, _ -> new Matrix4f());
    }
}
