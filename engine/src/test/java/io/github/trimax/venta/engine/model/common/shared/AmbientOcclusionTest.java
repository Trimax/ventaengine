package io.github.trimax.venta.engine.model.common.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.dto.scene.SceneAmbientOcclusionDTO;
import io.github.trimax.venta.engine.utils.ParsingUtil;

final class AmbientOcclusionTest {
    @Test
    void testOcclusionWithoutSettingsHasTheDefaults() {
        final var occlusion = new AmbientOcclusion(ParsingUtil.asObject("{}", SceneAmbientOcclusionDTO.class));

        assertEquals(AmbientOcclusion.DEFAULT_INTENSITY, occlusion.getIntensity());
        assertEquals(AmbientOcclusion.DEFAULT_RADIUS, occlusion.getRadius());
        assertEquals(AmbientOcclusion.DEFAULT_POWER, occlusion.getPower());
    }

    @Test
    void testSettingsAreRead() {
        final var occlusion = new AmbientOcclusion(ParsingUtil.asObject("{\"intensity\": 0.5, \"radius\": 4.5, \"power\": 2.0}", SceneAmbientOcclusionDTO.class));

        assertEquals(0.5f, occlusion.getIntensity());
        assertEquals(4.5f, occlusion.getRadius());
        assertEquals(2.f, occlusion.getPower());
    }

    @Test
    void testBadSettingsAreRefused() {
        assertThrows(IllegalArgumentException.class, () -> new AmbientOcclusion(-0.1f, 2.f, 1.5f));
        assertThrows(IllegalArgumentException.class, () -> new AmbientOcclusion(1.1f, 2.f, 1.5f));
        assertThrows(IllegalArgumentException.class, () -> new AmbientOcclusion(Float.NaN, 2.f, 1.5f));
        assertThrows(IllegalArgumentException.class, () -> new AmbientOcclusion(0.5f, 0.f, 1.5f));
        assertThrows(IllegalArgumentException.class, () -> new AmbientOcclusion(0.5f, -1.f, 1.5f));
        assertThrows(IllegalArgumentException.class, () -> new AmbientOcclusion(0.5f, Float.POSITIVE_INFINITY, 1.5f));
        assertThrows(IllegalArgumentException.class, () -> new AmbientOcclusion(0.5f, 2.f, 0.f));
        assertThrows(IllegalArgumentException.class, () -> new AmbientOcclusion(0.5f, 2.f, AmbientOcclusion.MAXIMAL_POWER + 1.f));
    }

    @Test
    void testLimitsAreAllowed() {
        assertDoesNotThrow(() -> new AmbientOcclusion(0.f, 0.01f, AmbientOcclusion.MAXIMAL_POWER), "No intensity switches the effect off");
        assertDoesNotThrow(() -> new AmbientOcclusion(1.f, 1000.f, 0.01f));
    }

    @Test
    void testRefusedSettingChangesNothing() {
        final var occlusion = new AmbientOcclusion(0.5f, 2.f, 1.5f);

        assertThrows(IllegalArgumentException.class, () -> occlusion.setRadius(-5.f));

        assertEquals(2.f, occlusion.getRadius());
    }
}
