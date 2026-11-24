package io.github.trimax.venta.engine.model.common.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.dto.scene.SceneLightShaftsDTO;
import io.github.trimax.venta.engine.utils.ParsingUtil;

final class LightShaftsTest {
    @Test
    void testShaftsWithoutSettingsHaveTheDefaults() {
        final var shafts = new LightShafts(ParsingUtil.asObject("{}", SceneLightShaftsDTO.class));

        assertEquals(LightShafts.DEFAULT_INTENSITY, shafts.getIntensity());
        assertEquals(LightShafts.DEFAULT_STEPS, shafts.getSteps());
        assertEquals(LightShafts.DEFAULT_ANISOTROPY, shafts.getAnisotropy());
    }

    @Test
    void testSettingsAreRead() {
        final var shafts = new LightShafts(ParsingUtil.asObject("{\"intensity\": 0.4, \"steps\": 64, \"anisotropy\": -0.2}", SceneLightShaftsDTO.class));

        assertEquals(0.4f, shafts.getIntensity());
        assertEquals(64, shafts.getSteps());
        assertEquals(-0.2f, shafts.getAnisotropy());
    }

    @Test
    void testBadSettingsAreRefused() {
        assertThrows(IllegalArgumentException.class, () -> new LightShafts(-0.1f, 32, 0.6f));
        assertThrows(IllegalArgumentException.class, () -> new LightShafts(Float.NaN, 32, 0.6f));
        assertThrows(IllegalArgumentException.class, () -> new LightShafts(1.f, LightShafts.MINIMAL_STEPS - 1, 0.6f));
        assertThrows(IllegalArgumentException.class, () -> new LightShafts(1.f, LightShafts.MAXIMAL_STEPS + 1, 0.6f));
        assertThrows(IllegalArgumentException.class, () -> new LightShafts(1.f, 32, 1.f), "The phase function would be infinite");
        assertThrows(IllegalArgumentException.class, () -> new LightShafts(1.f, 32, -1.f));
        assertThrows(IllegalArgumentException.class, () -> new LightShafts(1.f, 32, Float.NaN));
    }

    @Test
    void testLimitsOfTheStepsAreAllowed() {
        assertDoesNotThrow(() -> new LightShafts(1.f, LightShafts.MINIMAL_STEPS, 0.f));
        assertDoesNotThrow(() -> new LightShafts(1.f, LightShafts.MAXIMAL_STEPS, 0.f));
        assertDoesNotThrow(() -> new LightShafts(0.f, 32, 0.f), "A shaft without intensity switches the effect off");
    }

    @Test
    void testRefusedSettingChangesNothing() {
        final var shafts = new LightShafts(0.5f, 32, 0.6f);

        assertThrows(IllegalArgumentException.class, () -> shafts.setSteps(1));
        assertThrows(IllegalArgumentException.class, () -> shafts.setAnisotropy(2.f));

        assertEquals(32, shafts.getSteps());
        assertEquals(0.6f, shafts.getAnisotropy());
    }
}
