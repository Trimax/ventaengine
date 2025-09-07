package io.github.trimax.venta.engine.callbacks;

import io.github.trimax.venta.engine.controllers.WindowController;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

@Slf4j
@AllArgsConstructor
public final class WindowSizeCallback extends GLFWWindowSizeCallback implements AbstractCallback {
    private final WindowController windowController;

    @Override
    public void invoke(final long windowID, final int width, final int height) {
        windowController.get().setWidth(width);
        windowController.get().setHeight(height);
        log.info("Window resized: {}x{}", width, height);
    }
}
