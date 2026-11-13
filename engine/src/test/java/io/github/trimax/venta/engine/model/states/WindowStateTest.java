package io.github.trimax.venta.engine.model.states;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

final class WindowStateTest {
    @Test
    void testProjectionUsesWindowAspect() {
        final var state = new WindowState(null, 1, "test", 800, 400, 4);

        final var expected = new Matrix4f().perspective((float) Math.toRadians(60), 2.f, 0.1f, 1000f);
        assertEquals(expected, state.getProjectionMatrix(), "Projection must use the window aspect ratio");
    }

    @Test
    void testMinimizedWindowKeepsLastProjection() {
        final var state = new WindowState(null, 1, "test", 800, 600, 4);
        final var normal = new Matrix4f(state.getProjectionMatrix());

        state.setWidth(0);
        state.setHeight(0);
        final var minimized = state.getProjectionMatrix();

        assertTrue(minimized.isFinite(), "Projection of a minimized window must not contain NaN or infinity");
        assertEquals(normal, minimized, "The last valid projection must be kept");
    }

    @Test
    void testRestoredWindowUpdatesProjection() {
        final var state = new WindowState(null, 1, "test", 800, 600, 4);
        state.setHeight(0);
        state.getProjectionMatrix();

        state.setWidth(1000);
        state.setHeight(500);

        assertEquals(new Matrix4f().perspective((float) Math.toRadians(60), 2.f, 0.1f, 1000f), state.getProjectionMatrix(),
                "Projection must follow the restored size");
    }
}
