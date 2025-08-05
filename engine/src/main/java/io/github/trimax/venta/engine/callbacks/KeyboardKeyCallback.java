package io.github.trimax.venta.engine.callbacks;

import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.controllers.ConsoleController;
import io.github.trimax.venta.engine.controllers.WindowController;
import lombok.AllArgsConstructor;
import org.lwjgl.glfw.GLFWKeyCallback;

import static org.lwjgl.glfw.GLFW.*;

@AllArgsConstructor
public final class KeyboardKeyCallback extends GLFWKeyCallback implements AbstractCallback {
    private final ConsoleCommandQueue consoleCommandQueue;
    private final ConsoleController consoleController;
    private final WindowController windowController;

    @Override
    public void invoke(final long windowID, final int key, final int scancode, final int action, final int mods) {
        final var window = windowController.get();

        if (key == GLFW_KEY_F12 && action == GLFW_PRESS) {
            consoleController.toggle();
            return;
        }

        if (consoleController.isVisible()) {
            if (action == GLFW_PRESS || action == GLFW_REPEAT)
                consoleController.handle(key, consoleCommandQueue::add);
            return;
        }

        if (window.hasHandler())
            window.getHandler().onKey(key, scancode, action, mods);
    }
}
