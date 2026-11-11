package io.github.trimax.venta.engine.model.common.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector2f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.dto.common.WaveDTO;

final class WaveTest {
    @Test
    void testDirectionIsNormalized() {
        final var wave = new Wave(new WaveDTO(new Vector2f(1, 1), 0.5f, 0.3f, 10, 1));

        assertEquals(1.f, wave.getDirection().length(), 1e-6, "Direction must be a unit vector");
        assertEquals(wave.getDirection().x, wave.getDirection().y, 1e-6, "Direction must keep its orientation");
    }

    @Test
    void testDirectionSetterNormalizesAndCopies() {
        final var wave = new Wave(new Vector2f(1, 0), 0.5f, 0.3f, 10, 1);
        final var direction = new Vector2f(0, 3);

        wave.setDirection(direction);

        assertEquals(new Vector2f(0, 1), wave.getDirection(), "Direction must be normalized");
        assertEquals(new Vector2f(0, 3), direction, "The given vector must not be changed");
    }

    @Test
    void testOtherParametersAreKept() {
        final var wave = new Wave(new WaveDTO(new Vector2f(0.6f, 0.4f), 0.5f, 0.3f, 10, 2));

        assertEquals(0.5f, wave.getAmplitude(), "Amplitude");
        assertEquals(0.3f, wave.getSteepness(), "Steepness");
        assertEquals(10, wave.getLength(), "Length");
        assertEquals(2, wave.getSpeed(), "Speed");
    }

    @Test
    void testInvalidWavesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Wave(new Vector2f(0, 0), 1, 1, 10, 1), "Zero direction");
        assertThrows(IllegalArgumentException.class, () -> new Wave(new Vector2f(1, 0), 1, 1, 0, 1), "Zero length");
        assertThrows(IllegalArgumentException.class, () -> new Wave(new Vector2f(1, 0), 1, 1, -2, 1), "Negative length");
    }
}
