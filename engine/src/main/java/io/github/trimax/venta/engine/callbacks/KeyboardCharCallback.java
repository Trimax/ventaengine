package io.github.trimax.venta.engine.callbacks;

import io.github.trimax.venta.engine.model.entity.WindowEntity;
import lombok.AllArgsConstructor;
import org.lwjgl.glfw.GLFWCharCallback;

@AllArgsConstructor
public final class KeyboardCharCallback extends GLFWCharCallback implements AbstractCallback {
    private final WindowEntity window;

    @Override
    public void invoke(final long windowID, final int code) {
        if (!window.hasConsole())
            return;

        if (!window.getConsole().isVisible())
            return;

        window.getConsole().accept((char) code);
    }
}
