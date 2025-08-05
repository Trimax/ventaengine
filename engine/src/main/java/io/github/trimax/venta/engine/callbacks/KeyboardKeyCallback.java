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
        final var console = consoleController.get();
        final var window = windowController.get();

        if (key == GLFW_KEY_F12 && action == GLFW_PRESS) {
            console.toggle();
            return;
        }

        if (console.isVisible()) {
            if (action == GLFW_PRESS || action == GLFW_REPEAT)
                console.handle(key, consoleCommandQueue::add);
            return;
        }

        if (window.hasHandler())
            window.getHandler().onKey(key, scancode, action, mods);
    }
}
