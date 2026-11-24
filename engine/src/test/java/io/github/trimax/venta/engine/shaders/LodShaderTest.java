package io.github.trimax.venta.engine.shaders;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The dither of the cross fade of the levels of detail (common/lod.glsl) on the GPU: a level that fades in and the level that
 * fades out take the pixels from each other, together they have every pixel.
 */
final class LodShaderTest {
    private static final int SIDE = 24;

    private static ShaderHarness harness;

    @BeforeAll
    static void context() {
        GLTestContext.require();
        harness = new ShaderHarness();
    }

    private static boolean visible(final float fade, final int x, final int y) {
        final var result = harness.run(List.of("common/lod.glsl"), "uniform float testFade;\nuniform vec2 testPixel;",
                "outputColor = vec4(lodVisible(testFade, testPixel) ? 1.0 : 0.0, lodDither(testPixel), 0.0, 1.0);",
                Map.of("testFade", fade, "testPixel", new float[]{x + 0.5f, y + 0.5f}));

        return result[0] > 0.5f;
    }

    private static float share(final float fade) {
        var drawn = 0;
        for (int y = 0; y < SIDE; y++)
            for (int x = 0; x < SIDE; x++)
                if (visible(fade, x, y))
                    drawn++;

        return drawn / (float) (SIDE * SIDE);
    }

    @Test
    void testALevelThatFadesInTakesTheShareOfThePixelsOfItsFade() {
        assertEquals(0.f, share(0.f), "Nothing yet");
        assertEquals(0.25f, share(0.25f), 0.08f);
        assertEquals(0.5f, share(0.5f), 0.08f);
        assertEquals(0.75f, share(0.75f), 0.08f);
        assertEquals(1.f, share(1.f), "Everything at the end");
    }

    @Test
    void testALevelThatFadesOutLeavesTheShareOfItsFade() {
        assertEquals(1.f, share(-1.f), 0.02f, "Everything at the start");
        assertEquals(0.5f, share(-0.5f), 0.08f);
        assertEquals(0.f, share(-0.f), "Nothing at the end");
    }

    @Test
    void testTwoLevelsHaveEveryPixelOnce() {
        for (final var progress : new float[]{0.2f, 0.5f, 0.8f})
            for (int y = 0; y < SIDE; y++)
                for (int x = 0; x < SIDE; x++)
                    assertNotEquals(visible(progress, x, y), visible(-(1.f - progress), x, y),
                            "The pixel " + x + ", " + y + " at " + progress + " belongs to one level only");
    }

    @Test
    void testDitherIsFromZeroToOne() {
        for (int x = 0; x < SIDE; x++) {
            final var result = harness.run(List.of("common/lod.glsl"), "uniform vec2 testPixel;",
                    "outputColor = vec4(lodDither(testPixel), 0.0, 0.0, 1.0);", Map.of("testPixel", new float[]{x + 0.5f, 3.5f}));

            assertTrue(result[0] >= 0.f && result[0] < 1.f, "The dither is " + result[0]);
        }
    }
}
