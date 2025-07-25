package com.venta.engine.model.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

public record ObjectDTO(String type,
                        String name,
                        List<Vertex> vertices,
                        List<Facet> facets,
                        List<Edge> edges) {
    public boolean hasFacets() {
        return CollectionUtils.isNotEmpty(facets);
    }

    public boolean hasEdges() {
        return CollectionUtils.isNotEmpty(edges);
    }

    public int getFacetsArrayLength() {
        return hasFacets() ? 3 * facets.size() : 0;
    }

    public int getEdgesArrayLength() {
        return hasEdges() ? 2 * edges.size() : 0;
    }

    public float[] getVerticesArray() {
        final var tbnVectors = computeTBNVectors();

        final var packedArray = new float[vertices.size() * 18];
        for (int vertexID = 0; vertexID < vertices.size(); vertexID++) {
            final var vertex = vertices.get(vertexID);

            final var normal = Optional.ofNullable(tbnVectors.get(vertexID)).map(Triple::getLeft).orElse(null);
            final var tangent = Optional.ofNullable(tbnVectors.get(vertexID)).map(Triple::getMiddle).orElse(null);
            final var bitangent = Optional.ofNullable(tbnVectors.get(vertexID)).map(Triple::getRight).orElse(null);

            if (vertex.hasPosition()) {
                packedArray[18 * vertexID] = vertex.position().x();
                packedArray[18 * vertexID + 1] = vertex.position().y();
                packedArray[18 * vertexID + 2] = vertex.position().z();
            }

            if (vertex.hasNormal()) {
                packedArray[18 * vertexID + 3] = vertex.normal.x();
                packedArray[18 * vertexID + 4] = vertex.normal.y();
                packedArray[18 * vertexID + 5] = vertex.normal.z();
            }

            if (normal != null) {
                packedArray[18 * vertexID + 3] = normal.x();
                packedArray[18 * vertexID + 4] = normal.y();
                packedArray[18 * vertexID + 5] = normal.z();
            }

            if (vertex.hasTextureCoordinates()) {
                packedArray[18 * vertexID + 6] = vertex.textureCoordinates().x();
                packedArray[18 * vertexID + 7] = vertex.textureCoordinates().y();
            }

            if (vertex.hasColor()) {
                packedArray[18 * vertexID + 8] = vertex.color().x();
                packedArray[18 * vertexID + 9] = vertex.color().y();
                packedArray[18 * vertexID + 10] = vertex.color().z();
                packedArray[18 * vertexID + 11] = vertex.color().w();
            }

            if (tangent != null) {
                packedArray[18 * vertexID + 12] = tangent.x();
                packedArray[18 * vertexID + 13] = tangent.y();
                packedArray[18 * vertexID + 14] = tangent.z();
            }

            if (bitangent != null) {
                packedArray[18 * vertexID + 15] = bitangent.x();
                packedArray[18 * vertexID + 16] = bitangent.y();
                packedArray[18 * vertexID + 17] = bitangent.z();
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

    public int[] getEdgesArray() {
        final var packedArray = new int[edges.size() * 2];
        for (int edgeID = 0; edgeID < edges.size(); edgeID++) {
            final var edge = edges.get(edgeID);

            packedArray[2 * edgeID]     = edge.vertex1();
            packedArray[2 * edgeID + 1] = edge.vertex2();
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
        if (!hasFacets())
            return new HashMap<>();

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

    private Map<Integer, Triple<Vector3f, Vector3f, Vector3f>> computeTBNVectors() {
        if (!hasFacets())
            return new HashMap<>();

        final var faceNormals = computeFaceNormals();

        final var vertexNormals = new HashMap<Integer, List<Vector3f>>();
        final var vertexTangents = new HashMap<Integer, List<Vector3f>>();
        final var vertexBitangents = new HashMap<Integer, List<Vector3f>>();

        for (int facetIndex = 0; facetIndex < facets.size(); facetIndex++) {
            final var facet = facets.get(facetIndex);

            final Vertex vertex1 = vertices.get(facet.vertex1());
            final Vertex vertex2 = vertices.get(facet.vertex2());
            final Vertex vertex3 = vertices.get(facet.vertex3());

            final var normal = faceNormals.get(facetIndex);
            add(vertexNormals, normal, facet.vertex1);
            add(vertexNormals, normal, facet.vertex2);
            add(vertexNormals, normal, facet.vertex3);

            if (!hasTextureCoordinates(vertex1, vertex2, vertex3))
                continue;

            final var tangentBitangentData = prepareTangentBitangentData(vertex1, vertex2, vertex3);

            final var tangent = computeTangent(tangentBitangentData);
            add(vertexTangents, tangent, facet.vertex1);
            add(vertexTangents, tangent, facet.vertex2);
            add(vertexTangents, tangent, facet.vertex3);

            final var bitangent = computeBitangent(tangentBitangentData);
            add(vertexBitangents, bitangent, facet.vertex1);
            add(vertexBitangents, bitangent, facet.vertex2);
            add(vertexBitangents, bitangent, facet.vertex3);
        }

        final var normals = EntryStream.of(vertexNormals).mapValues(this::merge).toMap();
        final var tangents = EntryStream.of(vertexTangents).mapValues(this::merge).toMap();
        final var bitangents = EntryStream.of(vertexBitangents).mapValues(this::merge).toMap();

        final var tbnVectors = new HashMap<Integer, Triple<Vector3f, Vector3f, Vector3f>>();
        for (int vertexID = 0; vertexID < vertices.size(); vertexID++)
            tbnVectors.put(vertexID, Triple.of(normals.get(vertexID), tangents.get(vertexID), bitangents.get(vertexID)));

        return tbnVectors;
    }

    private Vector3f computeTangent(final TBData data) {
        return new Vector3f(
                data.f * (data.deltaV2 * data.edge1.x() - data.deltaV1 * data.edge2.x()),
                data.f * (data.deltaV2 * data.edge1.y() - data.deltaV1 * data.edge2.y()),
                data.f * (data.deltaV2 * data.edge1.z() - data.deltaV1 * data.edge2.z())
        );
    }

    private Vector3f computeBitangent(final TBData data) {
        return new Vector3f(
                data.f * (-data.deltaU2 * data.edge1.x() + data.deltaU1 * data.edge2.x()),
                data.f * (-data.deltaU2 * data.edge1.y() + data.deltaU1 * data.edge2.y()),
                data.f * (-data.deltaU2 * data.edge1.z() + data.deltaU1 * data.edge2.z())
        );
    }

    private TBData prepareTangentBitangentData(final Vertex vertex0, final Vertex vertex1, final Vertex vertex2) {
        final var pos1 = vertex0.position();
        final var pos2 = vertex1.position();
        final var pos3 = vertex2.position();

        final var uv1 = new Vector2f(vertex0.textureCoordinates().x(), vertex0.textureCoordinates().y());
        final var uv2 = new Vector2f(vertex1.textureCoordinates().x(), vertex1.textureCoordinates().y());
        final var uv3 = new Vector2f(vertex2.textureCoordinates().x(), vertex2.textureCoordinates().y());

        final var edge1 = pos2.sub(pos1, new Vector3f());
        final var edge2 = pos3.sub(pos1, new Vector3f());

        final var deltaU1 = uv2.x - uv1.x;
        final var deltaV1 = uv2.y - uv1.y;
        final var deltaU2 = uv3.x - uv1.x;
        final var deltaV2 = uv3.y - uv1.y;

        final var f = safeInverse(deltaU1 * deltaV2 - deltaU2 * deltaV1);
        return new TBData(edge1, edge2, deltaU1, deltaV1, deltaU2, deltaV2, f);
    }

    private boolean hasTextureCoordinates(final Vertex... vertices) {
        return StreamEx.of(vertices).allMatch(Vertex::hasTextureCoordinates);
    }

    private float safeInverse(final float value) {
        return (value == 0.0f) ? 1.0f : 1.0f / value;
    }

    private Vector3f merge(final List<Vector3f> vectors) {
        final var sum = new Vector3f();
        StreamEx.of(vectors).forEach(sum::add);

        return sum.normalize();
    }

    private void add(final Map<Integer, List<Vector3f>> vertexNormals, final Vector3f normal, final int vertexID) {
        if (!vertexNormals.containsKey(vertexID))
            vertexNormals.put(vertexID, new ArrayList<>());

        vertexNormals.get(vertexID).add(normal);
    }

    private record TBData(
            Vector3f edge1,
            Vector3f edge2,
            float deltaU1,
            float deltaV1,
            float deltaU2,
            float deltaV2,
            float f
    ) {}

    public record Vertex(Vector3f position,
                         Vector3f normal,
                         Vector2f textureCoordinates,
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
                        int vertex3) {}

    public record Edge(int vertex1,
                       int vertex2) {}
}
