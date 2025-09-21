package io.github.trimax.venta.engine.model.common.geo;

public record Geometry(int objectID, Buffer vertices, Buffer facets, Buffer edges) {
    public boolean isValid() {
        return objectID > 0;
    }

    public boolean isRenderable() {
        return isValid() && hasVertices() && (hasFacets() || hasEdges());
    }

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
        return buffer != null && buffer.isValid() && buffer.count() > 0;
    }
}
