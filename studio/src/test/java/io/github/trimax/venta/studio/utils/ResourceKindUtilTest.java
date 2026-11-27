package io.github.trimax.venta.studio.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.dto.EmitterDTO;
import io.github.trimax.venta.engine.model.dto.TerrainSurfaceDTO;
import io.github.trimax.venta.studio.model.outline.OutlineKind;

final class ResourceKindUtilTest {
    @Test
    void testFileOfAThingIsInTheFolderOfItsKind() {
        assertEquals("emitters/flame.json", ResourceKindUtil.pathOf(OutlineKind.Emitter, "flame.json").orElseThrow());
        assertEquals("soundsources/fire.json", ResourceKindUtil.pathOf(OutlineKind.Sound, "fire.json").orElseThrow());
        assertEquals("surfaces/terrain/beach.json", ResourceKindUtil.pathOf(OutlineKind.Terrain, "beach.json").orElseThrow());
    }

    @Test
    void testNothingIsMadeOfAFileForAKindThatIsNotAThingOrForNoFile() {
        assertTrue(ResourceKindUtil.pathOf(OutlineKind.Setting, "x.json").isEmpty());
        assertTrue(ResourceKindUtil.pathOf(OutlineKind.Object, "").isEmpty());
    }

    @Test
    void testPathTellsWhatTheFileIsFor() {
        final var target = ResourceKindUtil.of("emitters/campfire-flame.json").orElseThrow();

        assertEquals(OutlineKind.Emitter, target.kind());
        assertEquals("campfire-flame.json", target.file());
        assertEquals(EmitterDTO.class, target.type());
    }

    @Test
    void testFileInAFolderInsideTheFolderOfTheKindKeepsItsPath() {
        final var target = ResourceKindUtil.of("surfaces/terrain/hills/one.json").orElseThrow();

        assertEquals(OutlineKind.Terrain, target.kind());
        assertEquals("hills/one.json", target.file());
        assertEquals(TerrainSurfaceDTO.class, target.type());
    }

    @Test
    void testFileThatNoThingIsMadeOfHasNoTarget() {
        assertTrue(ResourceKindUtil.of("materials/chest.json").isEmpty());
        assertTrue(ResourceKindUtil.of("textures/a.png").isEmpty());
    }

    @Test
    void testEveryPathIsTakenBackToTheSameTarget() {
        for (final var kind : new OutlineKind[]{OutlineKind.Object, OutlineKind.Light, OutlineKind.Emitter, OutlineKind.Billboard, OutlineKind.Sound, OutlineKind.Terrain, OutlineKind.Water}) {
            final var path = ResourceKindUtil.pathOf(kind, "a.json").orElseThrow();
            assertEquals(kind, ResourceKindUtil.of(path).orElseThrow().kind(), path);
            assertNotNull(ResourceKindUtil.typeOf(kind), kind.name());
        }
    }

    @Test
    void testMaterialFileIsToldFromTheFilesOfThings() {
        assertEquals("chest.json", ResourceKindUtil.materialOf("materials/chest.json").orElseThrow());
        assertEquals("lod/rock.json", ResourceKindUtil.materialOf("materials/lod/rock.json").orElseThrow());
        assertTrue(ResourceKindUtil.materialOf("objects/chest.json").isEmpty());
        assertTrue(ResourceKindUtil.materialOf("materials/").isEmpty());
        assertTrue(ResourceKindUtil.of("materials/chest.json").isEmpty(), "No thing of a scene is made of a material");
    }
}
