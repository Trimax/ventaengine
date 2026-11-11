package io.github.trimax.venta.engine.parsers;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.dto.common.MeshDTO;

final class ObjMeshParserTest {
    private static final String SQUARE = """
            v 0 0 0
            v 1 0 0
            v 1 1 0
            v 0 1 0
            """;

    @Test
    void testTriangle() throws IOException {
        final var mesh = parse("""
                v 0 0 0
                v 1 0 0
                v 0 1 0
                f 1 2 3
                """);

        assertEquals(3, mesh.vertices().size(), "Vertices");
        assertEquals(1, mesh.facets().size(), "Facets");
        assertEquals(new MeshDTO.Facet(0, 1, 2), mesh.facets().getFirst(), "Facet indices must start at 0");
        assertEquals(3, mesh.edges().size(), "Edges");
    }

    @Test
    void testQuadHasNoDiagonalEdge() throws IOException {
        final var mesh = parse(SQUARE + "f 1 2 3 4\n");

        assertEquals(2, mesh.facets().size(), "A quad is two triangles");
        assertEquals(Set.of(edge(0, 1), edge(1, 2), edge(2, 3), edge(0, 3)), new HashSet<>(mesh.edges()), "Edges must be the outline only");
    }

    @Test
    void testPolygonIsTriangulated() throws IOException {
        final var mesh = parse("""
                v 0 0 0
                v 2 0 0
                v 3 1 0
                v 1 2 0
                v -1 1 0
                f 1 2 3 4 5
                """);

        assertEquals(3, mesh.facets().size(), "A pentagon is three triangles");
        assertEquals(5, mesh.edges().size(), "A pentagon has five edges");
    }

    @Test
    void testSharedEdgesAreNotDuplicated() throws IOException {
        final var mesh = parse(SQUARE + "f 1 2 3\nf 1 3 4\n");

        assertEquals(5, mesh.edges().size(), "The shared edge must be added once");
    }

    @Test
    void testNegativeIndicesAreRelative() throws IOException {
        final var absolute = parse(SQUARE + "f 2 3 4\n");
        final var relative = parse(SQUARE + "f -3 -2 -1\n");

        assertEquals(positions(absolute), positions(relative), "Relative indices must reference the same vertices");
    }

    @Test
    void testVerticesAreSharedBetweenFaces() throws IOException {
        final var mesh = parse(SQUARE + "f 1 2 3\nf 1 3 -1\n");

        assertEquals(4, mesh.vertices().size(), "Absolute and relative references to the same data must share a vertex");
    }

    @Test
    void testTextureCoordinatesAndNormals() throws IOException {
        final var mesh = parse("""
                v 0 0 0
                v 1 0 0
                v 0 1 0
                vt 0 0.25
                vn 0 0 1
                f 1/1/1 2/1/1 3//1
                """);

        final var first = mesh.vertices().getFirst();
        assertEquals(new Vector2f(0, 0.75f), first.textureCoordinates(), "V coordinate must be flipped for OpenGL");
        assertEquals(new Vector3f(0, 0, 1), first.normal(), "Normal must be read");
        assertNull(mesh.vertices().get(2).textureCoordinates(), "Vertex without texture coordinates");
        assertEquals(new Vector3f(0, 0, 1), mesh.vertices().get(2).normal(), "Normal with skipped texture coordinates");
    }

    @Test
    void testCommentsAndUnsupportedStatementsAreIgnored() throws IOException {
        final var mesh = parse("""
                # comment
                o object
                g group
                usemtl material
                s 1
                v 0 0 0
                v 1 0 0
                v 0 1 0

                f 1 2 3
                """);

        assertEquals(1, mesh.facets().size(), "Only the face must be read");
    }

    @Test
    void testInvalidIndicesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> parse(SQUARE + "f 1 2 5\n"), "Index after the last vertex");
        assertThrows(IllegalArgumentException.class, () -> parse(SQUARE + "f 1 2 0\n"), "Index 0");
        assertThrows(IllegalArgumentException.class, () -> parse(SQUARE + "f 1 2 -5\n"), "Relative index before the first vertex");
    }

    @Test
    void testDegenerateFacesAreIgnored() throws IOException {
        final var mesh = parse(SQUARE + "f 1 2\n");

        assertTrue(mesh.facets().isEmpty(), "A face needs at least three vertices");
    }

    private static MeshDTO parse(final String text) throws IOException {
        return ObjMeshParser.parse(new BufferedReader(new StringReader(text)));
    }

    private static MeshDTO.Edge edge(final int from, final int to) {
        return new MeshDTO.Edge(from, to);
    }

    private static Set<Vector3f> positions(final MeshDTO mesh) {
        final var positions = new HashSet<Vector3f>();
        mesh.vertices().forEach(vertex -> positions.add(vertex.position()));
        return positions;
    }
}
