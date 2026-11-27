package io.github.trimax.venta.studio.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.enums.GroupType;

final class ReferenceScanUtilTest {
    private static final String SCENE = """
            {
              "skybox": "sunset.json",
              "objects": [
                {"name": "chest.json", "object": "chest.json", "position": {"x": 1, "y": 2, "z": 3}},
                {"name": "Palm", "object": "palm.json"}
              ],
              "lights": [{"name": "Lamp", "light": "chest.json"}]
            }
            """;

    @Test
    void testValuesAreFoundWithTheFieldTheyBelongTo() {
        final var references = ReferenceScanUtil.references(SCENE);

        assertTrue(references.stream().anyMatch(reference -> "skybox".equals(reference.key()) && reference.value().equals("sunset.json")));
        assertTrue(references.stream().anyMatch(reference -> "object".equals(reference.key()) && reference.value().equals("palm.json")));
        assertTrue(references.stream().anyMatch(reference -> "name".equals(reference.key()) && reference.value().equals("Palm")));
    }

    @Test
    void testNamesOfFieldsAreNotValues() {
        final var references = ReferenceScanUtil.references("{\"object\": \"a.json\"}");

        assertEquals(1, references.size());
        assertEquals("a.json", references.getFirst().value());
    }

    @Test
    void testFileIsToldFromAFileOfAnotherGroupWithTheSameName() {
        // chest.json is an object, a light and the name of a thing: only the object is the one that is looked for
        final var places = ReferenceScanUtil.to(SCENE, GroupType.Object, "chest.json");

        assertEquals(1, places.size());
        assertEquals("object", places.getFirst().key());
        assertEquals(1, ReferenceScanUtil.to(SCENE, GroupType.Light, "chest.json").size());
        assertTrue(ReferenceScanUtil.to(SCENE, GroupType.Material, "chest.json").isEmpty());
    }

    @Test
    void testElementOfAListBelongsToTheFieldOfTheList() {
        final var json = "{\"textures\": [\"a.png\", \"b.png\"], \"other\": [\"a.png\"]}";

        final var places = ReferenceScanUtil.to(json, GroupType.Texture, "a.png");

        assertEquals(1, places.size(), "The list `other` doesn't name textures");
        assertEquals("textures", places.getFirst().key());
    }

    @Test
    void testTexturesOfAMaterialAndFacesOfACubemapAreFoundByWhatTheyAreFor() {
        final var material = "{\"textures\": {\"diffuse\": \"chest/d.png\", \"normal\": \"chest/n.png\"}}";
        final var cubemap = "{\"faces\": {\"Right\": \"sky/px.png\"}}";

        assertEquals(1, ReferenceScanUtil.to(material, GroupType.Texture, "chest/d.png").size());
        assertEquals(1, ReferenceScanUtil.to(material, GroupType.Texture, "chest/n.png").size());
        assertEquals(1, ReferenceScanUtil.to(cubemap, GroupType.Texture, "sky/px.png").size());
    }

    @Test
    void testFieldsOfTheFileOfAnObjectAreFoundAtAnyDepth() {
        final var json = "{\"program\": \"default.json\", \"root\": {\"name\": \"x\", \"value\": {\"mesh\": \"chest.obj\", \"material\": \"chest.json\"}}}";

        assertEquals(1, ReferenceScanUtil.to(json, GroupType.Mesh, "chest.obj").size());
        assertEquals(1, ReferenceScanUtil.to(json, GroupType.Material, "chest.json").size());
        assertEquals(1, ReferenceScanUtil.to(json, GroupType.Program, "default.json").size());
    }

    @Test
    void testOnlyTheNameThatIsAskedForIsFound() {
        assertTrue(ReferenceScanUtil.to(SCENE, GroupType.Object, "chest").isEmpty(), "A part of a name is not the name");
        assertTrue(ReferenceScanUtil.to(SCENE, GroupType.Object, "Chest.json").isEmpty(), "The letters have to be the same");
    }

    @Test
    void testReplacingKeepsEveryOtherCharacterOfTheText() {
        final var places = ReferenceScanUtil.to(SCENE, GroupType.Object, "chest.json");

        final var changed = ReferenceScanUtil.replace(SCENE, places, "treasure/box.json");

        assertEquals(SCENE.replace("\"object\": \"chest.json\"", "\"object\": \"treasure/box.json\""), changed);
        assertTrue(changed.contains("\"name\": \"chest.json\""), "The name of the thing is not a reference");
        assertTrue(changed.contains("\"light\": \"chest.json\""), "The light of the same name is another file");
    }

    @Test
    void testReplacingSeveralPlacesInOneText() {
        final var json = "{\"a\": {\"texture\": \"x.png\"}, \"b\": {\"texture\": \"x.png\"}, \"c\": {\"texture\": \"y.png\"}}";

        final var changed = ReferenceScanUtil.replace(json, ReferenceScanUtil.to(json, GroupType.Texture, "x.png"), "long/new name.png");

        assertEquals("{\"a\": {\"texture\": \"long/new name.png\"}, \"b\": {\"texture\": \"long/new name.png\"}, \"c\": {\"texture\": \"y.png\"}}", changed);
    }

    @Test
    void testEscapedQuotesAndBracketsInStringsDoNotConfuseTheScan() {
        final var json = "{\"note\": \"a \\\" [ { b\", \"material\": \"m.json\"}";

        assertEquals(1, ReferenceScanUtil.to(json, GroupType.Material, "m.json").size());
    }

    @Test
    void testTextThatIsNotCompleteDoesNotFail() {
        assertDoesNotThrow(() -> ReferenceScanUtil.references("{\"object\": \"a.js"));
        assertTrue(ReferenceScanUtil.references("").isEmpty());
    }

    @Test
    void testFieldsThatNameNoFileHaveNoGroup() {
        assertNull(ReferenceScanUtil.groupOf("name"));
        assertNull(ReferenceScanUtil.groupOf(null));
        assertEquals(GroupType.Material, ReferenceScanUtil.groupOf("material"));
        assertEquals(GroupType.Texture, ReferenceScanUtil.groupOf("diffuse"));
    }

    @Test
    void testNamesInsideOfAFolderAreFoundByTheirPrefix() {
        final var json = "{\"textures\": [\"chest/a.png\", \"chests/b.png\", \"chest\"], \"texture\": \"chest/c.png\"}";

        final var places = ReferenceScanUtil.inside(json, GroupType.Texture, "chest/");

        assertEquals(2, places.size());
        assertEquals("{\"textures\": [\"box/a.png\", \"chests/b.png\", \"chest\"], \"texture\": \"box/c.png\"}",
                ReferenceScanUtil.replaceEach(json, places, value -> "box/" + value.substring("chest/".length())));
    }
}
