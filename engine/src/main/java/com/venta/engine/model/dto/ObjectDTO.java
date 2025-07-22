package com.venta.engine.model.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;

import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

public record ObjectDTO(String type,
                        String name,
                        List<Vertex> vertices,
                        List<Facet> facets) {
    public float[] getVerticesArray() {
        final var normals = computeVertexNormals();

        final var packedArray = new float[vertices.size() * 12];
        for (int vertexID = 0; vertexID < vertices.size(); vertexID++) {
            final var vertex = vertices.get(vertexID);
            final var normal = normals.get(vertexID);

            if (vertex.hasPosition()) {
                packedArray[12 * vertexID] = vertex.position().x();
                packedArray[12 * vertexID + 1] = vertex.position().y();
                packedArray[12 * vertexID + 2] = vertex.position().z();
            }

            packedArray[12 * vertexID + 3] = normal.x();
            packedArray[12 * vertexID + 4] = normal.y();
            packedArray[12 * vertexID + 5] = normal.z();

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

    public float[] getFaceNormals() {
        final var faceNormalsMap = computeFaceNormals();

        final var faceNormals = new float[facets().size() * 3];
        for (int i = 0; i < faceNormalsMap.size(); i++) {
            final var normal = faceNormalsMap.get(i);

            faceNormals[i * 3]     = normal.x();
            faceNormals[i * 3 + 1] = normal.y();
            faceNormals[i * 3 + 2] = normal.z();
        }

        return faceNormals;
    }

    private Map<Integer, Vector3f> computeFaceNormals() {
        final var faceNormals = new HashMap<Integer, Vector3f>();
        for (int i = 0; i < facets.size(); i++) {
            final var facet = facets.get(i);

            final var vertex1 = vertices.get(facet.vertex1()).position();
            final var vertex2 = vertices.get(facet.vertex2()).position();
            final var vertex3 = vertices.get(facet.vertex3()).position();


            final var edge1 = vertex2.sub(vertex1, new Vector3f());
            final var edge2 = vertex3.sub(vertex1, new Vector3f());

            faceNormals.put(i, edge1.cross(edge2, new Vector3f()).normalize());
        }

        return faceNormals;
    }

    public Map<Integer, Vector3f> computeVertexNormals() {
        final var faceNormals = computeFaceNormals();

        final var vertexNormals = new HashMap<Integer, List<Vector3f>>();
        for (int facetIndex = 0; facetIndex < facets.size(); facetIndex++) {
            final var facet = facets.get(facetIndex);
            final var faceNormal = faceNormals.get(facetIndex);

            if (!vertexNormals.containsKey(facet.vertex1()))
                vertexNormals.put(facet.vertex1, new ArrayList<>());
            vertexNormals.get(facet.vertex1).add(faceNormal);

            add(vertexNormals, faceNormal, facet.vertex1);
            add(vertexNormals, faceNormal, facet.vertex2);
            add(vertexNormals, faceNormal, facet.vertex3);
        }

        return EntryStream.of(vertexNormals).mapValues(this::merge).toMap();
    }

    private Vector3f merge(final List<Vector3f> normals) {
        final var mergedNormal = new Vector3f();
        StreamEx.of(normals).forEach(mergedNormal::add);

        return mergedNormal.normalize();
    }

    private void add(final Map<Integer, List<Vector3f>> vertexNormals, final Vector3f normal, final int vertexID) {
        if (!vertexNormals.containsKey(vertexID))
            vertexNormals.put(vertexID, new ArrayList<>());

        vertexNormals.get(vertexID).add(normal);
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
