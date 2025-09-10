package io.github.trimax.venta.engine.model.common.geo;

import lombok.NonNull;

public record Grid(@NonNull float[] vertices,
                   @NonNull int[] indices,
                   int verticesCount,
                   int facetsCount) {
}
