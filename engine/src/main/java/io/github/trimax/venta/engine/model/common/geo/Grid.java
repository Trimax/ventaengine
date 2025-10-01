package io.github.trimax.venta.engine.model.common.geo;

public record Grid(float[] vertices, int[] facets,
                   int verticesCount, int facetsCount) {
}
