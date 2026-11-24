package io.github.trimax.venta.engine.model.common.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.dto.scene.SceneDirectionalLightDTO;
import io.github.trimax.venta.engine.utils.ParsingUtil;

final class DirectionalLightTest {
    private static final String LIGHT = """
            {
              "color": {"r": 1.0, "g": 0.5, "b": 0.25, "a": 1.0},
              "direction": {"x": -1.0, "y": -0.5, "z": 0.1},
              "intensity": 1.5%s
            }""";

    @Test
    void testShadowsAreOffByDefault() {
        final var light = new DirectionalLight(new Vector3f(1, 1, 1), new Vector3f(0, -1, 0), 1.f);

        assertFalse(light.isCastShadows(), "Existing scenes must stay as they were");
        assertEquals(DirectionalLight.DEFAULT_SHADOW_DISTANCE, light.getShadowDistance(), "The default distance");
    }

    @Test
    void testSceneWithoutShadowSettingsHasNoShadows() {
        final var light = new DirectionalLight(ParsingUtil.asObject(LIGHT.formatted(""), SceneDirectionalLightDTO.class));

        assertFalse(light.isCastShadows(), "A missing castShadows is false");
        assertEquals(DirectionalLight.DEFAULT_SHADOW_DISTANCE, light.getShadowDistance(), "A missing distance is the default");
        assertEquals(1.5f, light.getIntensity(), "The rest of the light is parsed as before");
        assertEquals(new Vector3f(-1.f, -0.5f, 0.1f), light.getDirection(), "The direction is parsed as before");
    }

    @Test
    void testSceneShadowSettingsAreParsed() {
        final var light = new DirectionalLight(ParsingUtil.asObject(
                LIGHT.formatted(", \"castShadows\": true, \"shadowDistance\": 300"), SceneDirectionalLightDTO.class));

        assertTrue(light.isCastShadows(), "castShadows");
        assertEquals(300.f, light.getShadowDistance(), "shadowDistance");
    }

    @Test
    void testShadowsCanBeSwitchedOffInAScene() {
        final var light = new DirectionalLight(ParsingUtil.asObject(
                LIGHT.formatted(", \"castShadows\": false"), SceneDirectionalLightDTO.class));

        assertFalse(light.isCastShadows(), "An explicit false");
    }

    @Test
    void testBadShadowDistanceOfASceneIsRejected() {
        for (final var distance : new String[]{"0", "-5", "1e39"})
            assertThrows(IllegalArgumentException.class, () -> new DirectionalLight(ParsingUtil.asObject(
                            LIGHT.formatted(", \"castShadows\": true, \"shadowDistance\": " + distance), SceneDirectionalLightDTO.class)),
                    "The distance " + distance);
    }

    @Test
    void testShadowDistanceIsChecked() {
        final var light = new DirectionalLight(new Vector3f(1, 1, 1), new Vector3f(0, -1, 0), 1.f);

        light.setShadowDistance(50.f);
        assertEquals(50.f, light.getShadowDistance(), "A valid distance");

        for (final var distance : new float[]{0.f, -1.f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY})
            assertThrows(IllegalArgumentException.class, () -> light.setShadowDistance(distance), "The distance " + distance);

        assertEquals(50.f, light.getShadowDistance(), "A rejected distance changes nothing");
    }

    @Test
    void testSunHasNoDiscByDefault() {
        final var light = new DirectionalLight(ParsingUtil.asObject(LIGHT.formatted(""), SceneDirectionalLightDTO.class));

        assertEquals(0.f, light.getDiscSize(), "Existing scenes draw no disc");
        assertEquals(DirectionalLight.DEFAULT_DISC_INTENSITY, light.getDiscIntensity());
    }

    @Test
    void testDiscSettingsAreParsed() {
        final var light = new DirectionalLight(ParsingUtil.asObject(
                LIGHT.formatted(", \"discSize\": 2.5, \"discIntensity\": 100"), SceneDirectionalLightDTO.class));

        assertEquals(2.5f, light.getDiscSize());
        assertEquals(100.f, light.getDiscIntensity());
    }

    @Test
    void testBadDiscSettingsAreRefused() {
        final var light = new DirectionalLight(new Vector3f(1, 1, 1), new Vector3f(0, -1, 0), 1.f);

        assertThrows(IllegalArgumentException.class, () -> light.setDiscSize(-1.f));
        assertThrows(IllegalArgumentException.class, () -> light.setDiscSize(DirectionalLight.MAXIMAL_DISC_SIZE + 1.f));
        assertThrows(IllegalArgumentException.class, () -> light.setDiscSize(Float.NaN));
        assertThrows(IllegalArgumentException.class, () -> light.setDiscIntensity(-1.f));
        assertThrows(IllegalArgumentException.class, () -> light.setDiscIntensity(Float.POSITIVE_INFINITY));
        assertEquals(0.f, light.getDiscSize(), "A refused value changes nothing");
    }
}
