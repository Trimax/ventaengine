package com.venta.engine.model.parsing;

import com.venta.engine.model.memory.BakedObject;
import com.venta.engine.model.memory.Facet;
import com.venta.engine.model.memory.Vertex;

import java.util.List;

public record VentaObject(String type, String name, List<Vertex> vertices, List<Facet> facets) {
    public float[] getVerticesArray() {
        final var packedArray = new float[vertices.size() * 12];
        for (int vertexID = 0; vertexID < vertices.size(); vertexID++) {
            final var vertex = vertices.get(vertexID);

            if (vertex.hasPosition()) {
                packedArray[12 * vertexID] = vertex.position().x();
                packedArray[12 * vertexID + 1] = vertex.position().y();
                packedArray[12 * vertexID + 2] = vertex.position().z();
            }

            if (vertex.hasNormal()) {
                packedArray[12 * vertexID + 3] = vertex.normal().x();
                packedArray[12 * vertexID + 4] = vertex.normal().y();
                packedArray[12 * vertexID + 5] = vertex.normal().z();
            }

            if (vertex.hasTextureCoordinates()) {
                packedArray[12 * vertexID + 6] = vertex.textureCoordinates().u();
                packedArray[12 * vertexID + 7] = vertex.textureCoordinates().v();
            }

            if (vertex.hasColor()) {
                packedArray[12 * vertexID + 8] = vertex.color().r();
                packedArray[12 * vertexID + 9] = vertex.color().g();
                packedArray[12 * vertexID + 10] = vertex.color().b();
                packedArray[12 * vertexID + 11] = vertex.color().a();
            }
        }

        return packedArray;
    }

    public int[] getFacesArray() {
        final var packedArray = new int[facets.size() * 3];
        for (int facetID = 0; facetID < facets.size(); facetID++) {
            final var facet = facets.get(facetID);

            packedArray[3 * facetID]     = facet.vertex1();
            packedArray[3 * facetID + 1] = facet.vertex2();
            packedArray[3 * facetID + 2] = facet.vertex3();
        }

        return packedArray;
    }

    public BakedObject bake() {
        return new BakedObject(getVerticesArray(), getFacesArray());
    }
}
