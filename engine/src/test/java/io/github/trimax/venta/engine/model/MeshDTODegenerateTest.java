package io.github.trimax.venta.engine.model;

import static io.github.trimax.venta.engine.definitions.DefinitionsBuffer.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.layouts.MeshVertexLayout;
import io.github.trimax.venta.engine.model.dto.common.MeshDTO;

final class MeshDTODegenerateTest {
    private static final int FLOATS = MeshVertexLayout.getFloatsCount();

    @Test
    void testZeroAreaFaceProducesNoNaN() {
        // Three collinear vertices
        final var mesh = new MeshDTO(List.of(
                vertex(new Vector3f(0, 0, 0), new Vector2f(0, 0)),
                vertex(new Vector3f(1, 0, 0), new Vector2f(1, 0)),
                vertex(new Vector3f(2, 0, 0), new Vector2f(0, 1))),
                List.of(new MeshDTO.Facet(0, 1, 2)), List.of());

        assertNoNaN(mesh.getVerticesArray());
    }

    @Test
    void testZeroAreaFaceDoesNotSpoilNeighbours() {
        // Vertex 0 is shared by a valid face (normal +Z) and a zero-area face
        final var mesh = new MeshDTO(List.of(
                vertex(new Vector3f(0, 0, 0), new Vector2f(0, 0)),
                vertex(new Vector3f(1, 0, 0), new Vector2f(1, 0)),
                vertex(new Vector3f(0, 1, 0), new Vector2f(0, 1)),
                vertex(new Vector3f(2, 0, 0), new Vector2f(1, 1))),
                List.of(new MeshDTO.Facet(0, 1, 2), new MeshDTO.Facet(0, 1, 3)), List.of());

        final var vertices = mesh.getVerticesArray();

        assertNoNaN(vertices);
        assertEquals(1.f, vertices[VERTEX_OFFSET_NORMAL_Z], 1e-5, "Normal of the shared vertex must come from the valid face");
    }

    @Test
    void testDegenerateTextureCoordinatesProduceNoNaN() {
        // All vertices have the same texture coordinates, so there is no tangent space
        final var mesh = new MeshDTO(List.of(
                vertex(new Vector3f(0, 0, 0), new Vector2f(0.5f, 0.5f)),
                vertex(new Vector3f(1, 0, 0), new Vector2f(0.5f, 0.5f)),
                vertex(new Vector3f(0, 1, 0), new Vector2f(0.5f, 0.5f))),
                List.of(new MeshDTO.Facet(0, 1, 2)), List.of());

        final var vertices = mesh.getVerticesArray();

        assertNoNaN(vertices);
        assertEquals(1.f, vertices[VERTEX_OFFSET_NORMAL_Z], 1e-5, "Normal must still be computed");
    }

    @Test
    void testOppositeNormalsProduceNoNaN() {
        // The same triangle with both windings: the face normals cancel out at every vertex
        final var mesh = new MeshDTO(List.of(
                vertex(new Vector3f(0, 0, 0), new Vector2f(0, 0)),
                vertex(new Vector3f(1, 0, 0), new Vector2f(1, 0)),
                vertex(new Vector3f(0, 1, 0), new Vector2f(0, 1))),
                List.of(new MeshDTO.Facet(0, 1, 2), new MeshDTO.Facet(0, 2, 1)), List.of());

        assertNoNaN(mesh.getVerticesArray());
    }

    private static MeshDTO.Vertex vertex(final Vector3f position, final Vector2f textureCoordinates) {
        return new MeshDTO.Vertex(position, null, textureCoordinates, null);
    }

    private static void assertNoNaN(final float[] vertices) {
        for (int i = 0; i < vertices.length; i++)
            assertFalse(Float.isNaN(vertices[i]) || Float.isInfinite(vertices[i]),
                    "Vertex " + i / FLOATS + " component " + i % FLOATS + " must be finite, got " + vertices[i]);
    }
}
