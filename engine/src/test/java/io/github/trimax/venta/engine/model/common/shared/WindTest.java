package io.github.trimax.venta.engine.model.common.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.dto.scene.SceneWindDTO;
import io.github.trimax.venta.engine.utils.ParsingUtil;

final class WindTest {
    private static final float EPSILON = 1e-6f;

    @Test
    void testDirectionIsNormalized() {
        final var wind = new Wind(3.f, 4.f, 1.f, 1.f);

        assertEquals(0.6f, wind.getDirection().x(), EPSILON);
        assertEquals(0.8f, wind.getDirection().y(), EPSILON);
    }

    @Test
    void testWindWithoutDirectionIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new Wind(0.f, 0.f, 1.f, 1.f));
        assertThrows(IllegalArgumentException.class, () -> new Wind(Float.NaN, 1.f, 1.f, 1.f));
    }

    @Test
    void testStrengthAndSpeedMustNotBeNegative() {
        assertThrows(IllegalArgumentException.class, () -> new Wind(1.f, 0.f, -1.f, 1.f));
        assertThrows(IllegalArgumentException.class, () -> new Wind(1.f, 0.f, 1.f, -1.f));
        assertThrows(IllegalArgumentException.class, () -> new Wind(1.f, 0.f, Float.POSITIVE_INFINITY, 1.f));

        assertDoesNotThrow(() -> new Wind(1.f, 0.f, 0.f, 0.f), "A calm is a wind of the strength 0");
    }

    @Test
    void testWindIsReadFromTheJsonOfAScene() {
        final var dto = ParsingUtil.asObject("{\"direction\": {\"x\": 0.0, \"y\": 5.0, \"z\": 2.0}, \"strength\": 0.8, \"speed\": 2.0}", SceneWindDTO.class);
        final var wind = new Wind(dto);

        assertEquals(0.f, wind.getDirection().x(), EPSILON);
        assertEquals(1.f, wind.getDirection().y(), EPSILON, "The y of the direction is ignored: the wind blows along the ground");
        assertEquals(0.8f, wind.getStrength(), EPSILON);
        assertEquals(2.f, wind.getSpeed(), EPSILON);
    }

    @Test
    void testMissingSettingsHaveDefaults() {
        final var wind = new Wind(ParsingUtil.asObject("{}", SceneWindDTO.class));

        assertEquals(1.f, wind.getDirection().x(), EPSILON);
        assertEquals(0.f, wind.getDirection().y(), EPSILON);
        assertTrue(wind.getStrength() > 0.f, "A wind that is described blows");
        assertEquals(1.f, wind.getSpeed(), EPSILON);
    }

    @Test
    void testSettersKeepTheRules() {
        final var wind = new Wind(1.f, 0.f, 1.f, 1.f);

        wind.setDirection(0.f, -5.f);
        assertEquals(-1.f, wind.getDirection().y(), EPSILON);

        assertThrows(IllegalArgumentException.class, () -> wind.setDirection(0.f, 0.f));
        assertEquals(-1.f, wind.getDirection().y(), EPSILON, "A refused direction changes nothing");
    }
}
