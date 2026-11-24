package io.github.trimax.venta.engine.model.common.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.dto.scene.SceneBloomDTO;
import io.github.trimax.venta.engine.utils.ParsingUtil;

final class BloomTest {
    @Test
    void testBloomWithoutSettingsHasTheDefaults() {
        final var bloom = new Bloom(ParsingUtil.asObject("{}", SceneBloomDTO.class));

        assertEquals(Bloom.DEFAULT_INTENSITY, bloom.getIntensity());
        assertEquals(Bloom.DEFAULT_THRESHOLD, bloom.getThreshold());
        assertEquals(Bloom.DEFAULT_SOFT_KNEE, bloom.getSoftKnee());
    }

    @Test
    void testSettingsAreRead() {
        final var bloom = new Bloom(ParsingUtil.asObject("{\"intensity\": 0.3, \"threshold\": 2.5, \"softKnee\": 0.1}", SceneBloomDTO.class));

        assertEquals(0.3f, bloom.getIntensity());
        assertEquals(2.5f, bloom.getThreshold());
        assertEquals(0.1f, bloom.getSoftKnee());
    }

    @Test
    void testBadSettingsAreRefused() {
        assertThrows(IllegalArgumentException.class, () -> new Bloom(-0.1f, 1.f, 0.5f));
        assertThrows(IllegalArgumentException.class, () -> new Bloom(Float.NaN, 1.f, 0.5f));
        assertThrows(IllegalArgumentException.class, () -> new Bloom(0.1f, -1.f, 0.5f));
        assertThrows(IllegalArgumentException.class, () -> new Bloom(0.1f, Float.POSITIVE_INFINITY, 0.5f));
        assertThrows(IllegalArgumentException.class, () -> new Bloom(0.1f, 1.f, 1.5f));
        assertThrows(IllegalArgumentException.class, () -> new Bloom(0.1f, 1.f, -0.5f));
    }

    @Test
    void testBloomWithNoIntensityIsAllowed() {
        assertDoesNotThrow(() -> new Bloom(0.f, 1.f, 0.5f), "It is how a bloom is switched off without taking it away");
    }

    @Test
    void testRefusedSettingChangesNothing() {
        final var bloom = new Bloom(0.2f, 1.f, 0.5f);

        assertThrows(IllegalArgumentException.class, () -> bloom.setIntensity(-1.f));

        assertEquals(0.2f, bloom.getIntensity());
    }
}
