package io.github.trimax.venta.engine.callbacks;

import io.github.trimax.venta.engine.controllers.WindowController;
import lombok.AllArgsConstructor;
import org.lwjgl.glfw.GLFWCursorPosCallback;

@AllArgsConstructor
public final class MouseCursorCallback extends GLFWCursorPosCallback implements AbstractCallback {
    private final WindowController windowController;

    @Override
    public void invoke(final long windowID, final double x, final double y) {
        if (windowController.get().hasHandler())
            windowController.get().getHandler().onMouseMove(x, y);
    }
}
