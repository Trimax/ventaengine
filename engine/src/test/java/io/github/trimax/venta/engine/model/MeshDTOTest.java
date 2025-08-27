package io.github.trimax.venta.engine.model;

import io.github.trimax.venta.engine.model.dto.MeshDTO;
import lombok.NonNull;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.trimax.venta.engine.definitions.Definitions.*;
import static org.junit.jupiter.api.Assertions.*;

public final class MeshDTOTest {
    @Test
    void testGetVerticesArray_simpleTriangle() {
        final var triangle = createTriangleDTO();

        final var verticesArray = triangle.getVerticesArray();
        assertNotNull(verticesArray, "Vertices array should not be null");
        assertEquals(COUNT_FLOATS_PER_VERTEX * triangle.vertices().size(), verticesArray.length, "The number of vertices must be correct");

        /* Checking backed vertices positions */
        verifyPosition(0, verticesArray, new Vector3f(0.f, 0.f, 0.f));
        verifyPosition(1, verticesArray, new Vector3f(1.f, 0.f, 0.f));
        verifyPosition(2, verticesArray, new Vector3f(0.f, 1.f, 0.f));

        /* Checking backed normals */
        for (int i = 0; i < triangle.vertices().size(); i++) {
            assertEquals(0f, verticesArray[i * COUNT_FLOATS_PER_VERTEX + VERTEX_OFFSET_NORMAL_X], 1e-5, "Vertex " + i + " normal x must be correct");
            assertEquals(0f, verticesArray[i * COUNT_FLOATS_PER_VERTEX + VERTEX_OFFSET_NORMAL_Y], 1e-5, "Vertex " + i + " normal y must be correct");
            assertEquals(1f, verticesArray[i * COUNT_FLOATS_PER_VERTEX + VERTEX_OFFSET_NORMAL_Z], 1e-5, "Vertex " + i + " normal z must be correct");
        }

        /* Checking backed tangents */
        for (int i = 0; i < triangle.vertices().size(); i++)
            assertTrue(verticesArray[i * COUNT_FLOATS_PER_VERTEX + VERTEX_OFFSET_TANGENT_X] > 0, "Vertex " + i + " tangent direction should be correct");

        /* Checking backed bitangents */
        for (int i = 0; i < triangle.vertices().size(); i++)
            assertTrue(verticesArray[i * COUNT_FLOATS_PER_VERTEX + VERTEX_OFFSET_BITANGENT_Y] > 0, "Vertex " + i + " bitangent direction should be correct");

        final var facesArray = triangle.getFacesArray();
        assertNotNull(verticesArray, "Faces array should not be null");
        assertEquals(COUNT_VERTICES_PER_FACET * triangle.facets().size(), facesArray.length, "The number of faces must be correct");

        for (int i = 0; i < triangle.facets().size(); i++)
            assertEquals(i, facesArray[i], "Face vertex " + i + " index must be correct");

        final var edgesArray = triangle.getEdgesArray();
        assertNotNull(edgesArray, "Edges array should not be null");
        assertEquals(0, edgesArray.length, "The number of edges must be correct");
    }

    private void verifyPosition(final int vertexID, final float[] verticesArray, final Vector3f expected) {
        verifyValue(vertexID, verticesArray[vertexID * COUNT_FLOATS_PER_VERTEX + VERTEX_OFFSET_POSITION_X], expected.x);
        verifyValue(vertexID, verticesArray[vertexID * COUNT_FLOATS_PER_VERTEX + VERTEX_OFFSET_POSITION_Y], expected.y);
        verifyValue(vertexID, verticesArray[vertexID * COUNT_FLOATS_PER_VERTEX + VERTEX_OFFSET_POSITION_Z], expected.z);
    }

    private void verifyValue(final int vertexID, final float actual, final float expected) {
        assertEquals(expected, actual, 1e-6, "Vertex " + vertexID + " position z must be correct");
    }

    private static MeshDTO createTriangleDTO() {
        final var vertex0 = createVertex(new Vector3f(0, 0, 0), new Vector2f(0, 0));
        final var vertex1 = createVertex(new Vector3f(1, 0, 0), new Vector2f(1, 0));
        final var vertex2 = createVertex(new Vector3f(0, 1, 0), new Vector2f(0, 1));

        return new MeshDTO(
                List.of(vertex0, vertex1, vertex2),
                List.of(new MeshDTO.Facet(0, 1, 2)),
                List.of()
        );
    }

    private static MeshDTO.Vertex createVertex(@NonNull final Vector3f position, @NonNull final Vector2f textureCoordinates) {
        return new MeshDTO.Vertex(position, null, textureCoordinates, null);
    }
}

