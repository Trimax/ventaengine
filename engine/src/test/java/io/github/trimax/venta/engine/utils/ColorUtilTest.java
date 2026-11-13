package io.github.trimax.venta.engine.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

final class ColorUtilTest {
    private static final float EPSILON = 1e-4f;

    @Test
    void testBlackAndWhiteDontChange() {
        assertEquals(0.f, ColorUtil.toLinear(0.f), EPSILON, "Black");
        assertEquals(1.f, ColorUtil.toLinear(1.f), EPSILON, "White");
        assertEquals(0.f, ColorUtil.toSRGB(0.f), EPSILON, "Black");
        assertEquals(1.f, ColorUtil.toSRGB(1.f), EPSILON, "White");
    }

    @Test
    void testKnownValues() {
        assertEquals(0.2140f, ColorUtil.toLinear(0.5f), EPSILON, "sRGB middle gray is about 21% of the light");
        assertEquals(0.7354f, ColorUtil.toSRGB(0.5f), EPSILON, "Half of the light is about 74% in sRGB");
        assertEquals(0.02f / 12.92f, ColorUtil.toLinear(0.02f), EPSILON, "Dark values are on the linear segment");
    }

    @Test
    void testCurveIsContinuousAtTheLimit() {
        assertEquals(ColorUtil.toLinear(0.04045f), ColorUtil.toLinear(0.04046f), 1e-5f, "No jump between the segments");
    }

    @Test
    void testRoundTrip() {
        for (float value = 0.f; value <= 2.f; value += 0.05f)
            assertEquals(value, ColorUtil.toSRGB(ColorUtil.toLinear(value)), EPSILON, "Round trip of " + value);
    }

    @Test
    void testBrightColorsFollowTheCurve() {
        assertTrue(ColorUtil.toLinear(2.f) > 4.f, "HDR colors above 1 keep growing");
    }

    @Test
    void testVectorsKeepAlpha() {
        final var linear = ColorUtil.toLinear(new Vector4f(0.5f, 1.f, 0.f, 0.3f), new Vector4f());

        assertEquals(new Vector4f(0.2140f, 1.f, 0.f, 0.3f).x, linear.x, EPSILON, "Red");
        assertEquals(1.f, linear.y, EPSILON, "Green");
        assertEquals(0.f, linear.z, EPSILON, "Blue");
        assertEquals(0.3f, linear.w, EPSILON, "Alpha isn't a color");

        final var destination = new Vector3f();
        assertSame(destination, ColorUtil.toLinear(new Vector3f(0.5f), destination), "The destination is returned");
        assertEquals(0.2140f, destination.x, EPSILON, "Vector channels are converted");
    }
}
