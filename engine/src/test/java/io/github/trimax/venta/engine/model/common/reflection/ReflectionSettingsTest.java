package io.github.trimax.venta.engine.model.common.reflection;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.dto.MaterialReflectionsDTO;
import io.github.trimax.venta.engine.utils.ParsingUtil;

final class ReflectionSettingsTest {
    @Test
    void testSettingsWithoutValuesHaveTheDefaults() {
        final var settings = new ReflectionSettings(ParsingUtil.asObject("{}", MaterialReflectionsDTO.class));

        assertEquals(ReflectionSettings.DEFAULT_RESOLUTION, settings.getResolution());
        assertEquals(ReflectionSettings.DEFAULT_REFRESH, settings.getRefresh());
        assertEquals(ReflectionSettings.DEFAULT_RANGE, settings.getRange());
        assertFalse(settings.isStatic());
    }

    @Test
    void testValuesAreRead() {
        final var settings = new ReflectionSettings(ParsingUtil.asObject("{\"resolution\": 256, \"refresh\": 0, \"range\": 12.5}", MaterialReflectionsDTO.class));

        assertEquals(256, settings.getResolution());
        assertEquals(12.5f, settings.getRange());
        assertTrue(settings.isStatic(), "Without a refresh the cube map is drawn once");
    }

    @Test
    void testBadValuesAreRefused() {
        assertThrows(IllegalArgumentException.class, () -> new ReflectionSettings(ReflectionSettings.MINIMAL_RESOLUTION - 1, 0.25f, 40.f));
        assertThrows(IllegalArgumentException.class, () -> new ReflectionSettings(ReflectionSettings.MAXIMAL_RESOLUTION + 1, 0.25f, 40.f));
        assertThrows(IllegalArgumentException.class, () -> new ReflectionSettings(128, -0.1f, 40.f));
        assertThrows(IllegalArgumentException.class, () -> new ReflectionSettings(128, Float.NaN, 40.f));
        assertThrows(IllegalArgumentException.class, () -> new ReflectionSettings(128, 0.25f, 0.f));
        assertThrows(IllegalArgumentException.class, () -> new ReflectionSettings(128, 0.25f, Float.POSITIVE_INFINITY));
    }

    @Test
    void testLimitsAreAllowed() {
        assertDoesNotThrow(() -> new ReflectionSettings(ReflectionSettings.MINIMAL_RESOLUTION, 0.f, 0.1f));
        assertDoesNotThrow(() -> new ReflectionSettings(ReflectionSettings.MAXIMAL_RESOLUTION, 10.f, 1000.f));
    }
}
