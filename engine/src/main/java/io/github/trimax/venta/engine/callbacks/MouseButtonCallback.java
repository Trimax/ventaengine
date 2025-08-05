package io.github.trimax.venta.engine.callbacks;

import io.github.trimax.venta.engine.controllers.WindowController;
import lombok.AllArgsConstructor;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

@AllArgsConstructor
public final class MouseButtonCallback extends GLFWMouseButtonCallback implements AbstractCallback {
    private final WindowController windowController;

    @Override
    public void invoke(final long windowID, final int button, final int action, final int mods) {
        windowController.get().handleMouseButton(button, action, mods);
    }
}
