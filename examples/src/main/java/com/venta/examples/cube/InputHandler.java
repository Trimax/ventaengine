package com.venta.examples.cube;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import java.util.HashSet;
import java.util.Set;

import com.venta.engine.interfaces.VentaInputHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class InputHandler implements VentaInputHandler {
    private final Set<Integer> pushedButtons = new HashSet<>();

    @Override
    public void onKey(final int key, final int scancode, final int action, final int mods) {
        if (action == GLFW_PRESS)
            pushedButtons.add(key);

        if (action == GLFW_RELEASE)
            pushedButtons.remove(key);
    }

    @Override
    public void onMouseMove(final double x, final double y) {
        log.info("Mouse: {} x {}", x, y);
    }

    @Override
    public void onMouseButton(final int button, final int action, final int mods) {
        log.info("Mouse button: {} action {} mods {}", button, action, mods);
    }

    public boolean isButtonPushed(final int button) {
        return pushedButtons.contains(button);
    }
}
