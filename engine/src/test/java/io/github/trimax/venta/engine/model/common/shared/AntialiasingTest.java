package io.github.trimax.venta.engine.model.common.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.enums.AntialiasingType;
import io.github.trimax.venta.engine.enums.FxaaQuality;
import io.github.trimax.venta.engine.model.dto.scene.SceneAntialiasingDTO;
import io.github.trimax.venta.engine.utils.ParsingUtil;

final class AntialiasingTest {
    @Test
    void testAntialiasingWithoutSettingsIsFxaaWithTheDefaults() {
        final var antialiasing = new Antialiasing(ParsingUtil.asObject("{}", SceneAntialiasingDTO.class));

        assertEquals(AntialiasingType.FXAA, antialiasing.getType());
        assertEquals(FxaaQuality.Medium, antialiasing.getQuality());
        assertEquals(Antialiasing.DEFAULT_SUBPIXEL, antialiasing.getSubpixel());
        assertEquals(Antialiasing.DEFAULT_EDGE_THRESHOLD, antialiasing.getEdgeThreshold());
        assertTrue(antialiasing.isActive());
    }

    @Test
    void testSettingsAreRead() {
        final var antialiasing = new Antialiasing(ParsingUtil.asObject(
                "{\"type\": \"FXAA\", \"quality\": \"High\", \"subpixel\": 0.5, \"edgeThreshold\": 0.25}", SceneAntialiasingDTO.class));

        assertEquals(FxaaQuality.High, antialiasing.getQuality());
        assertEquals(0.5f, antialiasing.getSubpixel());
        assertEquals(0.25f, antialiasing.getEdgeThreshold());
    }

    @Test
    void testAntialiasingOfTypeNoneIsKeptButNotActive() {
        final var antialiasing = new Antialiasing(ParsingUtil.asObject("{\"type\": \"None\", \"quality\": \"Low\"}", SceneAntialiasingDTO.class));

        assertFalse(antialiasing.isActive(), "It is how the antialiasing is switched off without taking it away");
        assertEquals(FxaaQuality.Low, antialiasing.getQuality());
    }

    @Test
    void testQualityGivesMoreStepsTheBetterItIs() {
        assertTrue(FxaaQuality.Low.getSteps() < FxaaQuality.Medium.getSteps());
        assertTrue(FxaaQuality.Medium.getSteps() < FxaaQuality.High.getSteps());
    }

    @Test
    void testBadSettingsAreRefused() {
        final var type = AntialiasingType.FXAA;
        final var quality = FxaaQuality.Medium;

        assertThrows(IllegalArgumentException.class, () -> new Antialiasing(type, quality, -0.1f, 0.125f));
        assertThrows(IllegalArgumentException.class, () -> new Antialiasing(type, quality, 1.1f, 0.125f));
        assertThrows(IllegalArgumentException.class, () -> new Antialiasing(type, quality, Float.NaN, 0.125f));
        assertThrows(IllegalArgumentException.class, () -> new Antialiasing(type, quality, 0.5f, 0.f));
        assertThrows(IllegalArgumentException.class, () -> new Antialiasing(type, quality, 0.5f, 0.6f));
        assertThrows(IllegalArgumentException.class, () -> new Antialiasing(type, quality, 0.5f, Float.NaN));
    }

    @Test
    void testRefusedSettingChangesNothing() {
        final var antialiasing = new Antialiasing(AntialiasingType.FXAA, FxaaQuality.Medium, 0.5f, 0.125f);

        assertThrows(IllegalArgumentException.class, () -> antialiasing.setSubpixel(2.f));
        assertThrows(IllegalArgumentException.class, () -> antialiasing.setEdgeThreshold(0.f));

        assertEquals(0.5f, antialiasing.getSubpixel());
        assertEquals(0.125f, antialiasing.getEdgeThreshold());
    }
}
