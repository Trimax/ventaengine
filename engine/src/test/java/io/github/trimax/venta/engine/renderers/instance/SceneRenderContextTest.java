package io.github.trimax.venta.engine.renderers.instance;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.trimax.venta.engine.model.entity.implementation.TestMeshes;

final class SceneRenderContextTest {
    @Test
    void testLightsAreBoundOncePerProgramAndFrame() {
        final var context = new SceneInstanceRenderer.SceneRenderContext();
        final var first = TestMeshes.program();
        final var second = TestMeshes.program();

        try {
            assertTrue(context.markLightsBound(first), "First instance of a program uploads the lights");
            assertFalse(context.markLightsBound(first), "Next instances of the same program skip it");
            assertTrue(context.markLightsBound(second), "Another program uploads its own lights, even if it is equal by value");

            context.close();

            assertTrue(context.markLightsBound(first), "The next frame uploads the lights again");
        } finally {
            context.destroy();
        }
    }
}
