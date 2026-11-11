package io.github.trimax.venta.engine.model.common.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

final class FogTest {
    @Test
    void testInvalidDistancesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Fog(-1, 10, new Vector3f()), "Negative minimal distance");
        assertThrows(IllegalArgumentException.class, () -> new Fog(10, 10, new Vector3f()), "Equal distances");
        assertThrows(IllegalArgumentException.class, () -> new Fog(10, 5, new Vector3f()), "Maximal below minimal");
    }

    @Test
    void testMaximalDistanceIsKeptAboveMinimal() {
        final var fog = new Fog(10, 20, new Vector3f());

        fog.setMaximalDistance(10);
        assertTrue(fog.getMaximalDistance() > fog.getMinimalDistance(), "Maximal distance must stay above minimal");

        fog.setMaximalDistance(5);
        assertTrue(fog.getMaximalDistance() > fog.getMinimalDistance(), "Maximal distance must stay above minimal");
    }

    @Test
    void testMovingMinimalDistanceForwardMovesMaximal() {
        final var fog = new Fog(10, 20, new Vector3f());

        fog.setMinimalDistance(30);

        assertEquals(30, fog.getMinimalDistance(), "Minimal distance must be set");
        assertTrue(fog.getMaximalDistance() > 30, "Maximal distance must be moved forward");
    }

    @Test
    void testMovingFogForwardAsTheExampleDoes() {
        final var fog = new Fog(10, 15, new Vector3f());

        fog.setMinimalDistance(40);
        fog.setMaximalDistance(45);

        assertEquals(40, fog.getMinimalDistance(), "Minimal distance");
        assertEquals(45, fog.getMaximalDistance(), "Maximal distance");
    }

    @Test
    void testNegativeMinimalDistanceIsClamped() {
        final var fog = new Fog(10, 20, new Vector3f());

        fog.setMinimalDistance(-5);

        assertEquals(0, fog.getMinimalDistance(), "Minimal distance must not be negative");
        assertEquals(20, fog.getMaximalDistance(), "Maximal distance must not change");
    }
}
