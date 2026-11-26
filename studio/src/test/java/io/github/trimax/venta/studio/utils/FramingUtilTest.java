package io.github.trimax.venta.studio.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

final class FramingUtilTest {
    @Test
    void testFrameIsTheMiddleOfTheBoxOfThePoints() {
        final var frame = FramingUtil.frame(List.of(new Vector3f(0.f, 0.f, 0.f), new Vector3f(20.f, 10.f, 0.f), new Vector3f(10.f, 2.f, 30.f)));

        assertEquals(10.f, frame.center().x, 1e-5f);
        assertEquals(5.f, frame.center().y, 1e-5f);
        assertEquals(15.f, frame.center().z, 1e-5f);
        assertEquals(0.5f * (float) Math.sqrt(20 * 20 + 10 * 10 + 30 * 30), frame.radius(), 1e-4f);
    }

    @Test
    void testSceneThatIsAPointIsLookedAtFromAFixedDistance() {
        final var frame = FramingUtil.frame(List.of(new Vector3f(4.f, 1.f, 2.f)));

        assertEquals(FramingUtil.MINIMAL_RADIUS, frame.radius());
        assertEquals(4.f, frame.center().x);
    }

    @Test
    void testNothingHasNoFrame() {
        assertNull(FramingUtil.frame(List.of()));
    }
}
