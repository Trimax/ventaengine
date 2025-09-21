package io.github.trimax.venta.engine.model.common.geo;

import lombok.NonNull;

public record Geometry(int objectID,

                       @NonNull Buffer vertices,
                       @NonNull Buffer facets,
                       @NonNull Buffer edges) {
    public boolean hasVertices() {
        return hasSomething(vertices);
    }

    public boolean hasFacets() {
        return hasSomething(facets);
    }

    public boolean hasEdges() {
        return hasSomething(edges);
    }

    private boolean hasSomething(final Buffer buffer) {
        return buffer.isValid() && buffer.count() > 0;
    }
}
