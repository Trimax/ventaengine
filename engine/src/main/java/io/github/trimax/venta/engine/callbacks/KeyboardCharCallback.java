package io.github.trimax.venta.engine.callbacks;

import io.github.trimax.venta.engine.controllers.ConsoleController;
import lombok.AllArgsConstructor;
import org.lwjgl.glfw.GLFWCharCallback;

@AllArgsConstructor
public final class KeyboardCharCallback extends GLFWCharCallback implements AbstractCallback {
    private final ConsoleController consoleController;

    @Override
    public void invoke(final long windowID, final int code) {
        if (!consoleController.isVisible())
            return;

        consoleController.accept((char) code);
    }
}
