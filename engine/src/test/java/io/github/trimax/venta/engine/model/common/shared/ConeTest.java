package io.github.trimax.venta.engine.model.common.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.dto.LightDTO;
import io.github.trimax.venta.engine.utils.ParsingUtil;

final class ConeTest {
    private static Cone parse(final String json) {
        return new Cone(ParsingUtil.asObject("{\"color\": {\"r\": 1, \"g\": 1, \"b\": 1, \"a\": 1}" + json + "}", LightDTO.class));
    }

    @Test
    void testConeWithoutAnglesHasTheDefaults() {
        final var cone = parse("");

        assertEquals(Cone.DEFAULT_INNER_ANGLE, cone.getInnerAngle());
        assertEquals(Cone.DEFAULT_OUTER_ANGLE, cone.getOuterAngle());
    }

    @Test
    void testAnglesAreRead() {
        final var cone = parse(", \"innerAngle\": 10, \"outerAngle\": 45");

        assertEquals(10.f, cone.getInnerAngle());
        assertEquals(45.f, cone.getOuterAngle());
    }

    @Test
    void testInnerAngleAloneWidensTheDefaultOuterOne() {
        final var cone = parse(", \"innerAngle\": 50");

        assertEquals(50.f, cone.getInnerAngle());
        assertEquals(50.f, cone.getOuterAngle(), "The outer angle is never narrower than the inner one");
    }

    @Test
    void testCosinesAreOfTheAngles() {
        final var cone = new Cone(60.f, 90.f - 5.f);

        assertEquals(0.5f, cone.getCosInner(), 1e-6f);
        assertEquals((float) Math.cos(Math.toRadians(85.f)), cone.getCosOuter(), 1e-6f);
        assertTrue(cone.getCosInner() > cone.getCosOuter(), "The wider the angle, the less the cosine");
    }

    @Test
    void testBadAnglesAreRefused() {
        assertThrows(IllegalArgumentException.class, () -> new Cone(0.f, 30.f));
        assertThrows(IllegalArgumentException.class, () -> new Cone(-5.f, 30.f));
        assertThrows(IllegalArgumentException.class, () -> new Cone(40.f, 30.f), "The inner angle is not wider than the outer one");
        assertThrows(IllegalArgumentException.class, () -> new Cone(20.f, Cone.MAXIMAL_OUTER_ANGLE + 1.f));
        assertThrows(IllegalArgumentException.class, () -> new Cone(Float.NaN, 30.f));
        assertThrows(IllegalArgumentException.class, () -> new Cone(20.f, Float.POSITIVE_INFINITY));
    }

    @Test
    void testEqualAnglesAreAllowed() {
        assertDoesNotThrow(() -> new Cone(30.f, 30.f), "It is a hard edge");
    }
}
