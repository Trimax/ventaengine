package io.github.trimax.venta.studio.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.dto.SceneDTO;
import io.github.trimax.venta.engine.utils.ParsingUtil;
import io.github.trimax.venta.studio.model.outline.OutlineKind;
import io.github.trimax.venta.studio.model.outline.OutlineNode;

final class OutlineUtilTest {
    private static final String SCENE = """
            {
              "objects": [
                {"name": "Chest", "object": "chest.json", "position": {"x": 1, "y": 2, "z": 3}, "angles": {"x": 0, "y": 0, "z": 0}, "scale": {"x": 1, "y": 1, "z": 1}},
                {"name": "Palm", "object": "palm.json", "position": {"x": -4, "y": 0, "z": 8}, "angles": {"x": 0, "y": 0, "z": 0}, "scale": {"x": 1, "y": 1, "z": 1}}
              ],
              "lights": [{"name": "Lamp", "light": "lamp.json", "position": {"x": 0, "y": 5, "z": 0}}],
              "soundSources": [{"name": "Surf", "soundSource": "surf.json", "position": {"x": 100, "y": 0, "z": 0}}],
              "fog": {"color": {"r": 1, "g": 1, "b": 1, "a": 1}, "density": 0.2},
              "exposure": 0.6,
              "skybox": "beach.json"
            }
            """;

    private static OutlineNode node(final OutlineNode parent, final String name) {
        return parent.children().stream().filter(child -> child.item().name().equals(name)).findFirst().orElseThrow();
    }

    @Test
    void testSceneIsTheRootAndHoldsTheCategoriesThatAreNotEmpty() {
        final var outline = OutlineUtil.build("beach.json", ParsingUtil.asObject(SCENE, SceneDTO.class));

        assertEquals(OutlineKind.Scene, outline.item().kind());
        assertEquals("beach.json", outline.item().name());
        assertEquals(List.of("Environment", "Objects", "Lights", "Sounds"), outline.children().stream().map(child -> child.item().name()).toList());
    }

    @Test
    void testThingsAreListedByTheirNamesWithTheFilesTheyAreMadeOf() {
        final var objects = node(OutlineUtil.build("s.json", ParsingUtil.asObject(SCENE, SceneDTO.class)), "Objects");

        assertEquals(2, objects.children().size());
        assertEquals("Chest", objects.children().get(0).item().name());
        assertEquals("chest.json", objects.children().get(0).item().detail());
        assertEquals(OutlineKind.Object, objects.children().get(0).item().kind());
        assertNotNull(objects.children().get(0).item().source(), "The inspector shows the part of the file");
    }

    @Test
    void testEnvironmentHasOnlyTheSettingsTheSceneHas() {
        final var environment = node(OutlineUtil.build("s.json", ParsingUtil.asObject(SCENE, SceneDTO.class)), "Environment");

        assertEquals(List.of("Skybox", "Fog", "Output"), environment.children().stream().map(child -> child.item().name()).toList());
        assertTrue(environment.children().stream().allMatch(child -> child.item().kind() == OutlineKind.Setting));
    }

    @Test
    void testEmptySceneHasNothingInIt() {
        final var outline = OutlineUtil.build("empty.json", ParsingUtil.asObject("{}", SceneDTO.class));

        assertTrue(outline.children().isEmpty());
    }

    @Test
    void testPositionsAreOfWhatIsWorthLookingAt() {
        final var positions = OutlineUtil.positions(ParsingUtil.asObject(SCENE, SceneDTO.class));

        assertEquals(2, positions.size(), "The objects only: the light and the sound are not where the scene is");
        assertEquals(1.f, positions.get(0).x);
    }

    @Test
    void testSceneWithoutObjectsIsLookedAtByTheRest() {
        final var scene = ParsingUtil.asObject("{\"lights\": [{\"name\": \"Lamp\", \"light\": \"l.json\", \"position\": {\"x\": 0, \"y\": 5, \"z\": 0}}]}", SceneDTO.class);

        assertEquals(1, OutlineUtil.positions(scene).size());
        assertTrue(OutlineUtil.positions(ParsingUtil.asObject("{}", SceneDTO.class)).isEmpty());
    }

    private static final String DUPLICATES = """
            {
              "objects": [
                {"name": "Tree", "object": "a.json", "position": {"x": 0, "y": 0, "z": 0}, "angles": {"x": 0, "y": 0, "z": 0}, "scale": {"x": 1, "y": 1, "z": 1}},
                {"name": "Rock", "object": "r.json", "position": {"x": 0, "y": 0, "z": 0}, "angles": {"x": 0, "y": 0, "z": 0}, "scale": {"x": 1, "y": 1, "z": 1}},
                {"name": "Tree", "object": "b.json", "position": {"x": 0, "y": 0, "z": 0}, "angles": {"x": 0, "y": 0, "z": 0}, "scale": {"x": 1, "y": 1, "z": 1}}
              ]
            }
            """;

    @Test
    void testThingsThatShareANameAreToldByTheirPlaceInTheFile() {
        final var outline = OutlineUtil.build("s.json", ParsingUtil.asObject(DUPLICATES, SceneDTO.class));

        assertEquals("a.json", OutlineUtil.at(outline, OutlineKind.Object, 0).detail());
        assertEquals("r.json", OutlineUtil.at(outline, OutlineKind.Object, 1).detail());
        assertEquals("b.json", OutlineUtil.at(outline, OutlineKind.Object, 2).detail());
        assertEquals("Tree", OutlineUtil.at(outline, OutlineKind.Object, 2).name());
        assertNull(OutlineUtil.at(outline, OutlineKind.Object, 3));
        assertNull(OutlineUtil.at(outline, OutlineKind.Light, 0), "Another kind");
    }

    @Test
    void testThingsKnowTheirPlacesAndSettingsHaveNone() {
        final var outline = OutlineUtil.build("s.json", ParsingUtil.asObject(SCENE, SceneDTO.class));

        assertTrue(OutlineUtil.items(outline, OutlineKind.Object).stream().allMatch(item -> item.isThing()));
        assertEquals(List.of(0, 1), OutlineUtil.items(outline, OutlineKind.Object).stream().map(item -> item.index()).toList());
        assertTrue(OutlineUtil.items(outline, OutlineKind.Setting).stream().noneMatch(item -> item.isThing()));
        assertFalse(outline.item().isThing());
    }

    @Test
    void testItemsOfAKindAreInTheOrderOfTheFile() {
        final var outline = OutlineUtil.build("s.json", ParsingUtil.asObject(DUPLICATES, SceneDTO.class));

        assertEquals(List.of("Tree", "Rock", "Tree"), OutlineUtil.items(outline, OutlineKind.Object).stream().map(item -> item.name()).toList());
        assertTrue(OutlineUtil.items(outline, OutlineKind.Light).isEmpty());
    }
}
