package com.venta.engine.model.dto;

import java.util.List;

import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;

public record ObjectDTO(String type,
                        String name,
                        List<Vertex> vertices,
                        List<Facet> facets) {
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
                packedArray[12 * vertexID + 6] = vertex.textureCoordinates().x();
                packedArray[12 * vertexID + 7] = vertex.textureCoordinates().y();
            }

            if (vertex.hasColor()) {
                packedArray[12 * vertexID + 8] = vertex.color().x();
                packedArray[12 * vertexID + 9] = vertex.color().y();
                packedArray[12 * vertexID + 10] = vertex.color().z();
                packedArray[12 * vertexID + 11] = vertex.color().w();
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

    public record Vertex(Vector3f position,
                         Vector3f normal,
                         Vector2i textureCoordinates,
                         Vector4f color) {
        public boolean hasPosition() {
            return position != null;
        }

        public boolean hasNormal() {
            return normal != null;
        }

        public boolean hasTextureCoordinates() {
            return textureCoordinates != null;
        }

        public boolean hasColor() {
            return color != null;
        }
    }

    public record Facet(int vertex1,
                        int vertex2,
                        int vertex3) {
    }
}
