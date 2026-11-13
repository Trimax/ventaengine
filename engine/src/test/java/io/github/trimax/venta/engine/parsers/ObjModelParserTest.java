package io.github.trimax.venta.engine.parsers;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.dto.common.MeshDTO;
import io.github.trimax.venta.engine.model.dto.common.ObjModelDTO;

final class ObjModelParserTest {
    /* A trunk triangle and two crown triangles, the crown has two materials */
    private static final String TREE = """
            mtllib tree.mtl
            v 0 0 0
            v 1 0 0
            v 0 1 0
            v 0 2 0
            v 1 2 0
            v 0 3 0
            o Trunk
            usemtl Bark
            f 1 2 3
            o Crown
            usemtl Leaves
            f 4 5 6
            usemtl Flowers
            f 4 6 5
            """;

    @Test
    void testPartPerGroupAndMaterial() throws IOException {
        final var model = parseModel(TREE);

        assertEquals(List.of("tree.mtl"), model.materialLibraries(), "Material libraries must be collected");
        assertEquals(List.of("Trunk", "Crown (Leaves)", "Crown (Flowers)"), model.parts().stream().map(ObjModelDTO.Part::name).toList(),
                "A group with several materials must have a part per material named after both");
        assertEquals(List.of("Bark", "Leaves", "Flowers"), model.parts().stream().map(ObjModelDTO.Part::material).toList(),
                "Parts must keep their materials");
    }

    @Test
    void testPartsHaveOwnVertices() throws IOException {
        final var crown = parseModel(TREE).parts().get(1).mesh();

        assertEquals(3, crown.vertices().size(), "A part must have only the vertices of its faces");
        assertEquals(new MeshDTO.Facet(0, 1, 2), crown.facets().getFirst(), "Indices must start at 0 in every part");
    }

    @Test
    void testFacesBeforeGroupsAndMaterials() throws IOException {
        final var model = parseModel("""
                v 0 0 0
                v 1 0 0
                v 0 1 0
                f 1 2 3
                """);

        assertEquals(1, model.parts().size(), "A file without groups must have one part");
        assertEquals("default", model.parts().getFirst().name(), "Faces before any group belong to the default group");
        assertNull(model.parts().getFirst().material(), "Faces before any material have no material");
        assertTrue(model.materialLibraries().isEmpty(), "A file without mtllib has no material libraries");
    }

    @Test
    void testGroupsWithoutFacesHaveNoParts() throws IOException {
        final var model = parseModel("""
                v 0 0 0
                v 1 0 0
                v 0 1 0
                g Empty
                usemtl Unused
                g Triangle
                f 1 2 3
                """);

        assertEquals(List.of("Triangle"), model.parts().stream().map(ObjModelDTO.Part::name).toList(), "Empty groups must be skipped");
        assertEquals("Unused", model.parts().getFirst().material(), "The material stays active across groups");
    }

    @Test
    void testOneMeshKeepsEverything() throws IOException {
        final var mesh = ObjMeshParser.parse(new BufferedReader(new StringReader(TREE)));

        assertEquals(3, mesh.facets().size(), "Without splitting all the faces must be in one mesh");
        assertEquals(6, mesh.vertices().size(), "Without splitting the vertices must be shared");
    }

    private static ObjModelDTO parseModel(final String text) throws IOException {
        return ObjMeshParser.parseModel(new BufferedReader(new StringReader(text)));
    }
}
