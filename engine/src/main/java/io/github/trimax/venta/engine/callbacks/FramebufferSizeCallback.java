package io.github.trimax.venta.engine.callbacks;

import io.github.trimax.venta.engine.controllers.WindowController;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;

import static org.lwjgl.opengl.GL11C.glViewport;

@Slf4j
@AllArgsConstructor
public final class FramebufferSizeCallback extends GLFWFramebufferSizeCallback implements AbstractCallback {
    private final WindowController windowController;

    @Override
    public void invoke(final long windowID, final int width, final int height) {
        // The projection is computed from the window size on every frame, see WindowState
        glViewport(0, 0, width, height);
        log.info("Window resized: {}x{}", width, height);
    }
}
