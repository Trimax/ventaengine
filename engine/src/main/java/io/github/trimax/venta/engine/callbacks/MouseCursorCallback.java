package io.github.trimax.venta.engine.callbacks;

import io.github.trimax.venta.engine.model.entities.WindowEntity;
import lombok.AllArgsConstructor;
import org.lwjgl.glfw.GLFWCursorPosCallback;

@AllArgsConstructor
public final class MouseCursorCallback extends GLFWCursorPosCallback implements AbstractCallback {
    private final WindowEntity window;

    @Override
    public void invoke(final long windowID, final double x, final double y) {
        if (window.hasHandler())
            window.getHandler().onMouseMove(x, y);
    }
}
