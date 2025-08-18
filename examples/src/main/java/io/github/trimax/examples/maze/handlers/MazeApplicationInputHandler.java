package io.github.trimax.examples.maze.handlers;

import io.github.trimax.examples.maze.state.MazeApplicationState;
import io.github.trimax.venta.engine.interfaces.VentaEngineInputHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.lwjgl.glfw.GLFW.*;

@Slf4j
@AllArgsConstructor
public final class MazeApplicationInputHandler implements VentaEngineInputHandler {
    private final MazeApplicationState state;

    @Override
    public void onKey(final int key, final int scancode, final int action, final int mods) {
        if (action == GLFW_PRESS)
            state.getPushedButtons().add(key);

        if (action == GLFW_RELEASE)
            state.getPushedButtons().remove(key);
    }

    @Override
    public void onMouseButton(final int button, final int action, final int mods) {
        log.info("Mouse button: {} action {} mods {}", button, action, mods);
    }
}
