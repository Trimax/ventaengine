package io.github.trimax.venta.engine.model.dto;

import static io.github.trimax.venta.engine.definitions.DefinitionsCommon.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Vector2f;
import org.joml.Vector3f;

import io.github.trimax.venta.engine.layouts.MeshVertexLayout;
import io.github.trimax.venta.engine.model.common.dto.Color;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

public record MeshDTO(List<Vertex> vertices,
                      List<Facet> facets,
                      List<Edge> edges) {
    public boolean hasFacets() {
        return CollectionUtils.isNotEmpty(facets);
    }

    public boolean hasEdges() {
        return CollectionUtils.isNotEmpty(edges);
    }

    public int getFacetsArrayLength() {
        return hasFacets() ? COUNT_VERTICES_PER_FACET * facets.size() : 0;
    }

    public int getEdgesArrayLength() {
        return hasEdges() ? COUNT_VERTICES_PER_EDGE * edges.size() : 0;
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
                packedArray[MeshVertexLayout.getFloatsCount() * vertexID + VERTEX_OFFSET_POSITION_X] = vertex.position().x();
                packedArray[MeshVertexLayout.getFloatsCount() * vertexID + VERTEX_OFFSET_POSITION_Y] = vertex.position().y();
                packedArray[MeshVertexLayout.getFloatsCount() * vertexID + VERTEX_OFFSET_POSITION_Z] = vertex.position().z();
            }

            if (vertex.hasNormal()) {
                packedArray[MeshVertexLayout.getFloatsCount() * vertexID + VERTEX_OFFSET_NORMAL_X] = vertex.normal.x();
                packedArray[MeshVertexLayout.getFloatsCount() * vertexID + VERTEX_OFFSET_NORMAL_Y] = vertex.normal.y();
                packedArray[MeshVertexLayout.getFloatsCount() * vertexID + VERTEX_OFFSET_NORMAL_Z] = vertex.normal.z();
            }

            if (normal != null) {
                packedArray[MeshVertexLayout.getFloatsCount() * vertexID + VERTEX_OFFSET_NORMAL_X] = normal.x();
                packedArray[MeshVertexLayout.getFloatsCount() * vertexID + VERTEX_OFFSET_NORMAL_Y] = normal.y();
                packedArray[MeshVertexLayout.getFloatsCount() * vertexID + VERTEX_OFFSET_NORMAL_Z] = normal.z();
            }

            if (tangent != null) {
                packedArray[MeshVertexLayout.getFloatsCount() * vertexID + VERTEX_OFFSET_TANGENT_X] = tangent.x();
                packedArray[MeshVertexLayout.getFloatsCount() * vertexID + VERTEX_OFFSET_TANGENT_Y] = tangent.y();
                packedArray[MeshVertexLayout.getFloatsCount() * vertexID + VERTEX_OFFSET_TANGENT_Z] = tangent.z();
            }

            if (bitangent != null) {
                packedArray[MeshVertexLayout.getFloatsCount() * vertexID + VERTEX_OFFSET_BITANGENT_X] = bitangent.x();
                packedArray[MeshVertexLayout.getFloatsCount() * vertexID + VERTEX_OFFSET_BITANGENT_Y] = bitangent.y();
                packedArray[MeshVertexLayout.getFloatsCount() * vertexID + VERTEX_OFFSET_BITANGENT_Z] = bitangent.z();
            }

            if (vertex.hasTextureCoordinates()) {
                packedArray[MeshVertexLayout.getFloatsCount() * vertexID + VERTEX_OFFSET_TEXTURE_COORDINATES_U] = vertex.textureCoordinates().x();
                packedArray[MeshVertexLayout.getFloatsCount() * vertexID + VERTEX_OFFSET_TEXTURE_COORDINATES_V] = vertex.textureCoordinates().y();
            }

            if (vertex.hasColor()) {
                packedArray[MeshVertexLayout.getFloatsCount() * vertexID + VERTEX_OFFSET_COLOR_R] = vertex.color().r();
                packedArray[MeshVertexLayout.getFloatsCount() * vertexID + VERTEX_OFFSET_COLOR_G] = vertex.color().g();
                packedArray[MeshVertexLayout.getFloatsCount() * vertexID + VERTEX_OFFSET_COLOR_B] = vertex.color().b();
                packedArray[MeshVertexLayout.getFloatsCount() * vertexID + VERTEX_OFFSET_COLOR_A] = vertex.color().a();
            }
        }

        return packedArray;
    }

    public int[] getFacesArray() {
        final var packedArray = new int[facets.size() * COUNT_VERTICES_PER_FACET];
        for (int facetID = 0; facetID < facets.size(); facetID++) {
            final var facet = facets.get(facetID);

            packedArray[COUNT_VERTICES_PER_FACET * facetID + FACET_OFFSET_VERTEX_1] = facet.vertex1();
            packedArray[COUNT_VERTICES_PER_FACET * facetID + FACET_OFFSET_VERTEX_2] = facet.vertex2();
            packedArray[COUNT_VERTICES_PER_FACET * facetID + FACET_OFFSET_VERTEX_3] = facet.vertex3();
        }

        return packedArray;
    }

    public int[] getEdgesArray() {
        final var packedArray = new int[edges.size() * COUNT_VERTICES_PER_EDGE];
        for (int edgeID = 0; edgeID < edges.size(); edgeID++) {
            final var edge = edges.get(edgeID);

            packedArray[COUNT_VERTICES_PER_EDGE * edgeID + EDGE_OFFSET_VERTEX_1]     = edge.vertex1();
            packedArray[COUNT_VERTICES_PER_EDGE * edgeID + EDGE_OFFSET_VERTEX_2] = edge.vertex2();
        }

        return packedArray;
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
                         Color color) {
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
