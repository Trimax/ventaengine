package io.github.trimax.venta.engine.model.instance.implementation;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.enums.LightType;
import io.github.trimax.venta.engine.model.common.shared.Cone;

final class SpotLightInstanceTest {
    @Test
    void testLightWithoutAConeIsAPointLightShiningDown() {
        final var light = TestObjects.light("point", new Vector3f(), 10.f, false);

        assertEquals(LightType.Point, light.getType());
        assertNull(light.getCone());
        assertEquals(new Vector3f(0.f, -1.f, 0.f), new Vector3f(light.getDirection()));
    }

    @Test
    void testConeMakesASpotLight() {
        final var light = TestObjects.spot("spot", new Vector3f(), new Vector3f(1, 0, 0), 10.f, 20.f, 30.f, false);

        assertEquals(LightType.Spot, light.getType());
        assertEquals(20.f, light.getCone().getOuterAngle());

        light.setCone(null);
        assertEquals(LightType.Point, light.getType(), "Without the cone it is a point light again");
    }

    @Test
    void testDirectionIsNormalized() {
        final var light = TestObjects.spot("spot", new Vector3f(), new Vector3f(0, 0, 5), 10.f, 20.f, 30.f, false);

        assertEquals(1.f, light.getDirection().length(), 1e-6f);
        assertEquals(1.f, light.getDirection().z(), 1e-6f);
    }

    @Test
    void testBadDirectionIsRefusedAndKeepsTheOldOne() {
        final var light = TestObjects.spot("spot", new Vector3f(), new Vector3f(0, 0, 1), 10.f, 20.f, 30.f, false);

        assertThrows(IllegalArgumentException.class, () -> light.setDirection(new Vector3f()));
        assertThrows(IllegalArgumentException.class, () -> light.setDirection(new Vector3f(Float.NaN, 0, 0)));
        assertThrows(IllegalArgumentException.class, () -> light.setDirection(new Vector3f(Float.POSITIVE_INFINITY, 0, 0)));

        assertEquals(1.f, light.getDirection().z(), 1e-6f);
    }

    @Test
    void testLightKeepsItsOwnCopyOfTheDirection() {
        final var direction = new Vector3f(0, 0, 1);
        final var light = TestObjects.spot("spot", new Vector3f(), direction, 10.f, 20.f, 30.f, false);

        direction.set(1, 0, 0);

        assertEquals(1.f, light.getDirection().z(), 1e-6f);
        assertNotNull(new Cone(1.f, 2.f));
    }
}
