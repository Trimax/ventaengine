package io.github.trimax.venta.engine.model.common.geo;

public record Geometry(int vertexArrayObjectID,

                       int verticesBufferID,
                       int facetsBufferID,
                       int edgesBufferID,

                       int countVertices,
                       int countFacets,
                       int countEdges,

                       int lengthArrayVertices,
                       int lengthArrayFacets,
                       int lengthArrayEdges) {
    public boolean hasFacets() {
        return facetsBufferID > 0;
    }

    public boolean hasEdges() {
        return edgesBufferID > 0;
    }
}
