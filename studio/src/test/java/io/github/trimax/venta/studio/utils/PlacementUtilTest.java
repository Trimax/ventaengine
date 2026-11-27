package io.github.trimax.venta.studio.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Optional;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.engine.model.dto.SceneDTO;
import io.github.trimax.venta.engine.utils.ParsingUtil;
import io.github.trimax.venta.studio.model.outline.OutlineKind;

final class PlacementUtilTest {
    @Test
    void testFilesOfTheGroupsOfThingsBecomeThings() {
        assertEquals(Optional.of(OutlineKind.Object), PlacementUtil.kindOf(GroupType.Object));
        assertEquals(Optional.of(OutlineKind.Sound), PlacementUtil.kindOf(GroupType.AudioSource));
        assertEquals(Optional.of(OutlineKind.Terrain), PlacementUtil.kindOf(GroupType.TerrainSurface));
        assertEquals(Optional.of(OutlineKind.Water), PlacementUtil.kindOf(GroupType.WaterSurface));
    }

    @Test
    void testOtherFilesAreNotThingsOfAScene() {
        assertEquals(Optional.empty(), PlacementUtil.kindOf(GroupType.Texture));
        assertEquals(Optional.empty(), PlacementUtil.kindOf(GroupType.Material));
        assertEquals(Optional.empty(), PlacementUtil.kindOf(GroupType.Scene));
        assertEquals(Optional.empty(), PlacementUtil.kindOf((GroupType) null));
    }

    @Test
    void testKindOfAFileOfTheProjectIsByItsFolderAndItsExtension(@TempDir final Path project) {
        assertEquals(Optional.of(OutlineKind.Object), PlacementUtil.kindOf(project, project.resolve("objects/trees/pine.json")));
        assertEquals(Optional.empty(), PlacementUtil.kindOf(project, project.resolve("objects/trees/readme.md")), "Not a file of a thing");
        assertEquals(Optional.empty(), PlacementUtil.kindOf(project, project.resolve("textures/sand.json")));
        assertEquals(Optional.empty(), PlacementUtil.kindOf(project, project.resolve("notes/pine.json")), "Out of the folders of the groups");
    }

    @Test
    void testResourceIsThePathInsideTheFolderOfTheGroup(@TempDir final Path project) {
        assertEquals("trees/pine.json", PlacementUtil.resourceOf(project, project.resolve("objects/trees/pine.json")));
        assertEquals("beach.json", PlacementUtil.resourceOf(project, project.resolve("surfaces/terrain/beach.json")));
        assertEquals("sea.json", PlacementUtil.resourceOf(project, project.resolve("surfaces/water/sea.json")));
        assertThrows(IllegalArgumentException.class, () -> PlacementUtil.resourceOf(project, project.resolve("notes/a.json")));
    }

    @Test
    void testEveryKindOfAThingHasItsGroup() {
        for (final var kind : EnumSet.of(OutlineKind.Object, OutlineKind.Light, OutlineKind.Emitter, OutlineKind.Billboard, OutlineKind.Sound, OutlineKind.Terrain, OutlineKind.Water))
            assertEquals(Optional.of(kind), PlacementUtil.kindOf(PlacementUtil.groupOf(kind)));
    }

    /** What the studio writes for a new thing must be what the engine reads: the engine refuses a thing that lacks a field it needs */
    @Test
    void testEveryNewThingIsAThingTheEngineReads() {
        final var position = new Vector3f(1.f, 2.f, 3.f);
        final var scene = ParsingUtil.asObject("""
                {"objects": [%s], "lights": [%s], "emitters": [%s], "billboards": [%s], "soundSources": [%s], "terrainSurfaces": [%s], "waterSurfaces": [%s]}
                """.formatted(entry(OutlineKind.Object, position), entry(OutlineKind.Light, position), entry(OutlineKind.Emitter, position),
                entry(OutlineKind.Billboard, position), entry(OutlineKind.Sound, position), entry(OutlineKind.Terrain, position),
                entry(OutlineKind.Water, position)), SceneDTO.class);

        assertEquals("Thing", scene.objects().getFirst().name());
        assertEquals("a.json", scene.objects().getFirst().object());
        assertEquals(new Vector3f(1.f), scene.objects().getFirst().scale());
        assertEquals(position, scene.lights().getFirst().position());
        assertEquals("a.json", scene.emitters().getFirst().emitter());
        assertEquals(1.f, scene.billboards().getFirst().size().x);
        assertEquals("a.json", scene.soundSources().getFirst().soundSource());
        assertEquals("a.json", scene.terrainSurfaces().getFirst().terrainSurface());
        assertEquals("a.json", scene.waterSurfaces().getFirst().waterSurface());
    }

    private static String entry(final OutlineKind kind, final Vector3f position) {
        return PlacementUtil.entryFor(kind, "a.json", "Thing", position).toString();
    }

    @Test
    void testKindThatIsNotAThingHasNoEntry() {
        assertThrows(IllegalArgumentException.class, () -> PlacementUtil.entryFor(OutlineKind.Setting, "a.json", "Thing", new Vector3f()));
    }
}
