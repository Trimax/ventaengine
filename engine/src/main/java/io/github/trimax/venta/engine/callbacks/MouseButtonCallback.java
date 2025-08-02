package io.github.trimax.venta.engine.callbacks;

import io.github.trimax.venta.engine.model.entities.WindowEntity;
import lombok.AllArgsConstructor;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

@AllArgsConstructor
public final class MouseButtonCallback extends GLFWMouseButtonCallback implements AbstractCallback {
    private final WindowEntity window;

    @Override
    public void invoke(final long windowID, final int button, final int action, final int mods) {
        if (window.hasHandler())
            window.getHandler().onMouseButton(button, action, mods);
    }
}
