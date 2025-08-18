package io.github.trimax.venta.engine.callbacks;

import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.controllers.ConsoleController;
import io.github.trimax.venta.engine.controllers.KeyboardController;
import io.github.trimax.venta.engine.controllers.WindowController;
import lombok.AllArgsConstructor;
import org.lwjgl.glfw.GLFWKeyCallback;

import static org.lwjgl.glfw.GLFW.*;

@AllArgsConstructor
public final class KeyboardKeyCallback extends GLFWKeyCallback implements AbstractCallback {
    private final ConsoleCommandQueue consoleCommandQueue;
    private final KeyboardController keyboardController;
    private final ConsoleController consoleController;
    private final WindowController windowController;

    @Override
    public void invoke(final long windowID, final int key, final int scancode, final int action, final int mods) {
        if (key == GLFW_KEY_F12 && action == GLFW_PRESS) {
            keyboardController.get().clear();
            consoleController.toggle();
            return;
        }

        if (consoleController.isVisible()) {
            if (action == GLFW_PRESS || action == GLFW_REPEAT)
                consoleController.handle(key, consoleCommandQueue::add);
            return;
        }

        if (action == GLFW_PRESS)
            keyboardController.get().push(key);

        if (action == GLFW_RELEASE)
            keyboardController.get().release(key);

        windowController.get().handleKeyboardKey(key, scancode, action, mods);
    }
}
