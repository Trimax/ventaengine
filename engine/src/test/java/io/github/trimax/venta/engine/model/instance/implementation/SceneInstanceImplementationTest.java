package io.github.trimax.venta.engine.model.instance.implementation;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.enums.ToneMapping;
import io.github.trimax.venta.engine.model.entity.implementation.TestMeshes;
import io.github.trimax.venta.engine.model.prefabs.implementation.TestPrefabs;

final class SceneInstanceImplementationTest {
    @Test
    void testInstanceCanBeAddedToOneScene() {
        final var scene = scene("first");
        final var light = light();

        scene.add(light);
        scene.add(light);

        assertTrue(scene.contains(light), "Instance must be in the scene");
        assertSame(scene, light.getParent(), "Instance must reference its scene");
        assertEquals(1, scene.getLights().size(), "Adding to the same scene again changes nothing");
    }

    @Test
    void testInstanceCanNotBeAddedToAnotherScene() {
        final var first = scene("first");
        final var second = scene("second");
        final var light = light();
        first.add(light);

        final var error = assertThrows(IllegalStateException.class, () -> second.add(light), "Second scene must be rejected");

        assertTrue(error.getMessage().contains("first"), "Message must name the scene the instance is in");
        assertFalse(second.contains(light), "Instance must not be added to the second scene");
    }

    @Test
    void testInstanceCanBeMovedAfterRemove() {
        final var first = scene("first");
        final var second = scene("second");
        final var light = light();
        first.add(light);

        first.remove(light);
        second.add(light);

        assertFalse(first.contains(light), "Instance must be removed from the first scene");
        assertSame(second, light.getParent(), "Instance must reference the new scene");
        assertTrue(second.contains(light), "Instance must be in the second scene");
    }

    @Test
    void testDetachedSceneReleasesItsInstances() {
        final var deleted = scene("deleted");
        final var other = scene("other");
        final var light = light();
        deleted.add(light);

        deleted.detachAll();
        other.add(light);

        assertSame(other, light.getParent(), "Instance of a deleted scene can be added to another scene");
        assertTrue(deleted.getLights().isEmpty(), "Deleted scene must be empty");
    }

    @Test
    void testRemoveFromAnotherSceneIsIgnored() {
        final var first = scene("first");
        final var second = scene("second");
        final var light = light();
        first.add(light);

        second.remove(light);

        assertSame(first, light.getParent(), "Removing from a scene the instance is not in must change nothing");
        assertTrue(first.contains(light), "Instance must stay in its scene");
    }

    @Test
    void testSetParentKeepsOneScene() {
        final var first = scene("first");
        final var second = scene("second");
        final var light = light();

        light.setParent(first);
        light.setParent(first);
        assertSame(first, light.getParent(), "Setting the same scene again is allowed");

        assertThrows(IllegalStateException.class, () -> light.setParent(second), "Another scene must be rejected");
        assertSame(first, light.getParent(), "Parent must not change after the rejection");

        light.setParent(null);
        light.setParent(second);
        assertSame(second, light.getParent(), "After detaching the instance can get another scene");
    }

    @Test
    void testExposureAndToneMappingDefaults() {
        final var scene = scene("scene");

        assertEquals(1.f, scene.getExposure(), "The exposure 1 keeps the light");
        assertEquals(ToneMapping.Neutral, scene.getToneMapping(), "Neutral tone mapping keeps the authored colors");
    }

    @Test
    void testExposureIsNotNegative() {
        final var scene = scene("scene");

        scene.setExposure(2.5f);
        assertEquals(2.5f, scene.getExposure(), "The exposure must be set");

        scene.setExposure(-1.f);
        assertEquals(0.f, scene.getExposure(), "A negative exposure is no light");
    }

    @Test
    void testToneMappingIsSet() {
        final var scene = scene("scene");

        scene.setToneMapping(ToneMapping.ACES);
        assertEquals(ToneMapping.ACES, scene.getToneMapping(), "The tone mapping must be set");
        assertThrows(NullPointerException.class, () -> scene.setToneMapping(null), "The tone mapping is required");
    }

    private static SceneInstanceImplementation scene(final String name) {
        return new SceneInstanceImplementation(name);
    }

    private static LightInstanceImplementation light() {
        final var gizmo = new GizmoInstanceImplementation("gizmo", TestMeshes.program(), TestMeshes.cube(1),
                new Vector3f(), new Vector3f(), new Vector3f(1));
        return new LightInstanceImplementation("light", TestPrefabs.light(), gizmo);
    }
}
