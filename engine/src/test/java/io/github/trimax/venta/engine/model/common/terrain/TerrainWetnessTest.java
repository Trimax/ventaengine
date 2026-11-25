package io.github.trimax.venta.engine.model.common.terrain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.dto.terrain.TerrainWetnessDTO;
import io.github.trimax.venta.engine.utils.ParsingUtil;

final class TerrainWetnessTest {
    @Test
    void testWetnessWithoutSettingsHasTheDefaults() {
        final var wetness = new TerrainWetness(ParsingUtil.asObject("{}", TerrainWetnessDTO.class));

        assertEquals(0.f, wetness.getLevel());
        assertEquals(TerrainWetness.DEFAULT_HEIGHT, wetness.getHeight());
        assertEquals(TerrainWetness.DEFAULT_DARKENING, wetness.getDarkening());
        assertEquals(TerrainWetness.DEFAULT_GLOSSINESS, wetness.getGlossiness());
        assertEquals(0.f, wetness.getSwashAmplitude(), "No swash unless asked");
        assertEquals(TerrainWetness.DEFAULT_SWASH_PERIOD, wetness.getSwashPeriod());
    }

    @Test
    void testSettingsAreRead() {
        final var wetness = new TerrainWetness(ParsingUtil.asObject(
                "{\"level\": -2, \"height\": 1.5, \"darkening\": 0.3, \"glossiness\": 0.8, \"swashAmplitude\": 0.4, \"swashPeriod\": 9}", TerrainWetnessDTO.class));

        assertEquals(-2.f, wetness.getLevel());
        assertEquals(1.5f, wetness.getHeight());
        assertEquals(0.3f, wetness.getDarkening());
        assertEquals(0.8f, wetness.getGlossiness());
        assertEquals(0.4f, wetness.getSwashAmplitude());
        assertEquals(9.f, wetness.getSwashPeriod());
    }

    @Test
    void testBadSettingsAreRefused() {
        assertThrows(IllegalArgumentException.class, () -> new TerrainWetness(Float.NaN, 1.f, 0.5f, 0.5f, 0.f, 8.f));
        assertThrows(IllegalArgumentException.class, () -> new TerrainWetness(0.f, 0.f, 0.5f, 0.5f, 0.f, 8.f), "No height");
        assertThrows(IllegalArgumentException.class, () -> new TerrainWetness(0.f, -1.f, 0.5f, 0.5f, 0.f, 8.f));
        assertThrows(IllegalArgumentException.class, () -> new TerrainWetness(0.f, 1.f, 1.5f, 0.5f, 0.f, 8.f));
        assertThrows(IllegalArgumentException.class, () -> new TerrainWetness(0.f, 1.f, 0.5f, -0.1f, 0.f, 8.f));
        assertThrows(IllegalArgumentException.class, () -> new TerrainWetness(0.f, 1.f, 0.5f, 0.5f, -1.f, 8.f));
        assertThrows(IllegalArgumentException.class, () -> new TerrainWetness(0.f, 1.f, 0.5f, 0.5f, 0.f, 0.f), "No period");
    }
}
