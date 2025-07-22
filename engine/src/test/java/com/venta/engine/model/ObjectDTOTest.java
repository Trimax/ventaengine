package com.venta.engine.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import com.venta.engine.model.dto.ObjectDTO;

public final class ObjectDTOTest {
    private static final ObjectDTO triangle;

    static {
        final var vertices = List.of(
                new ObjectDTO.Vertex(new Vector3f(0, 0, 0), null, null, null), // A
                new ObjectDTO.Vertex(new Vector3f(1, 0, 0), null, null, null), // B
                new ObjectDTO.Vertex(new Vector3f(0, 1, 0), null, null, null)  // C
        );

        triangle = new ObjectDTO("test", "triangle", vertices, List.of(new ObjectDTO.Facet(0, 1, 2)));
    }

    @Test
    public void testBaking() {
        final var vertices = triangle.getVerticesArray();
        assertNotNull(vertices, "Vertices array should not be null");
        assertEquals(12 * triangle.vertices().size(), vertices.length, "The number of vertices must be correct");

        final var facets = triangle.getFacesArray();
        assertNotNull(facets, "Faces array should not be null");
        assertEquals(3 * triangle.facets().size(), facets.length, "The number of facets must be correct");
    }

    @Test
    public void testSingleTriangleNormals() {
        final var vertices = List.of(
                new ObjectDTO.Vertex(new Vector3f(0, 0, 0), null, null, null), // A
                new ObjectDTO.Vertex(new Vector3f(1, 0, 0), null, null, null), // B
                new ObjectDTO.Vertex(new Vector3f(0, 1, 0), null, null, null)  // C
        );

        final var triangle = new ObjectDTO("test", "triangle", vertices, List.of(new ObjectDTO.Facet(0, 1, 2)));

        final var faceNormals = triangle.getFaceNormals();
        assertEquals(3, faceNormals.length, "Face normal should have 3 components");
        assertArrayEquals(new float[] { 0f, 0f, 1f }, faceNormals, 1e-6f, "Face normal must be (0, 0, 1)");

        final var vertexNormals = triangle.computeVertexNormals();
        assertEquals(3, vertexNormals.size(), "There should be 3 vertex normals");

        for (int i = 0; i < 3; i++) {
            final var normal = vertexNormals.get(i);

            assertNotNull(normal, "Normal should not be null for vertex " + i);
            assertEquals(0f, normal.x(), 1e-6f, "Normal[" + i + "].x must be correct");
            assertEquals(0f, normal.y(), 1e-6f, "Normal[" + i + "].y must be correct");
            assertEquals(1f, normal.z(), 1e-6f, "Normal[" + i + "].z must be correct");
        }
    }
}
