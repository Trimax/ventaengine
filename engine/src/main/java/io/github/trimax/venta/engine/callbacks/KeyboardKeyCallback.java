package io.github.trimax.venta.engine.callbacks;

import io.github.trimax.venta.engine.model.entity.WindowEntity;
import lombok.AllArgsConstructor;
import org.lwjgl.glfw.GLFWKeyCallback;

import static org.lwjgl.glfw.GLFW.*;

@AllArgsConstructor
public final class KeyboardKeyCallback extends GLFWKeyCallback implements AbstractCallback {
    private final WindowEntity window;

    @Override
    public void invoke(final long windowID, final int key, final int scancode, final int action, final int mods) {
        if (key == GLFW_KEY_F12 && action == GLFW_PRESS && window.hasConsole()) {
            window.getConsole().toggle();
            return;
        }

        if (window.hasConsole() && window.getConsole().isVisible()) {
            if (action == GLFW_PRESS || action == GLFW_REPEAT)
                window.getConsole().handle(key, window.getConsoleQueue()::add);
            return;
        }

        if (window.hasHandler())
            window.getHandler().onKey(key, scancode, action, mods);
    }
}
