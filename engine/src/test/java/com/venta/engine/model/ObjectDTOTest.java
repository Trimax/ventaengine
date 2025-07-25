package com.venta.engine.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import com.venta.engine.model.dto.ObjectDTO;

public final class ObjectDTOTest {
    @Test
    void testGetVerticesArray_simpleTriangle() {
        final var triangle = createTriangleDTO();

        final var verticesArray = triangle.getVerticesArray();
        assertNotNull(verticesArray, "Vertices array should not be null");
        assertEquals(18 * triangle.vertices().size(), verticesArray.length, "The number of vertices must be correct");

        /* Checking backed vertices positions */
        assertEquals(0f, verticesArray[0], 1e-6, "Vertex 1 position x must be correct");
        assertEquals(0f, verticesArray[1], 1e-6, "Vertex 1 position y must be correct");
        assertEquals(0f, verticesArray[2], 1e-6, "Vertex 1 position z must be correct");

        assertEquals(1f, verticesArray[18], 1e-6, "Vertex 2 position x must be correct");
        assertEquals(0f, verticesArray[19], 1e-6, "Vertex 2 position x must be correct");
        assertEquals(0f, verticesArray[20], 1e-6, "Vertex 2 position x must be correct");

        assertEquals(0f, verticesArray[36], 1e-6, "Vertex 3 position x must be correct");
        assertEquals(1f, verticesArray[37], 1e-6, "Vertex 3 position x must be correct");
        assertEquals(0f, verticesArray[38], 1e-6, "Vertex 3 position x must be correct");

        /* Checking backed normals */
        for (int i = 0; i < triangle.vertices().size(); i++) {
            assertEquals(0f, verticesArray[i * 18 + 3], 1e-5, "Vertex " + i + " normal x must be correct");
            assertEquals(0f, verticesArray[i * 18 + 4], 1e-5, "Vertex " + i + " normal y must be correct");
            assertEquals(1f, verticesArray[i * 18 + 5], 1e-5, "Vertex " + i + " normal z must be correct");
        }

        /* Checking backed tangents */
        for (int i = 0; i < triangle.vertices().size(); i++)
            assertTrue(verticesArray[i * 18 + 12] > 0, "Vertex " + i + " tangent direction should be correct");

        /* Checking backed bitangents */
        for (int i = 0; i < triangle.vertices().size(); i++)
            assertTrue(verticesArray[i * 18 + 16] > 0, "Vertex " + i + " bitangent direction should be correct");

        final var facesArray = triangle.getFacesArray();
        assertNotNull(verticesArray, "Faces array should not be null");
        assertEquals(3 * triangle.facets().size(), facesArray.length, "The number of faces must be correct");

        for (int i = 0; i < triangle.facets().size(); i++)
            assertEquals(i, facesArray[i], "Face vertex " + i + " index must be correct");

        final var edgesArray = triangle.getEdgesArray();
        assertNotNull(edgesArray, "Edges array should not be null");
        assertEquals(0, edgesArray.length, "The number of edges must be correct");
    }

    private static ObjectDTO createTriangleDTO() {
        final var vertex0 = createVertex(new Vector3f(0, 0, 0), new Vector2f(0, 0));
        final var vertex1 = createVertex(new Vector3f(1, 0, 0), new Vector2f(1, 0));
        final var vertex2 = createVertex(new Vector3f(0, 1, 0), new Vector2f(0, 1));

        return new ObjectDTO(
                "triangle",
                "test",
                List.of(vertex0, vertex1, vertex2),
                List.of(new ObjectDTO.Facet(0, 1, 2)),
                List.of()
        );
    }

    private static ObjectDTO.Vertex createVertex(final Vector3f position, final Vector2f textureCoordinates) {
        return new ObjectDTO.Vertex(position, null, textureCoordinates, null);
    }
}

