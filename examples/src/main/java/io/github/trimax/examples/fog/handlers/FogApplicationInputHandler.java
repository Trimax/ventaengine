package io.github.trimax.examples.fog.handlers;

import io.github.trimax.examples.fog.state.FogApplicationState;
import io.github.trimax.venta.engine.interfaces.VentaEngineInputHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

@Slf4j
@AllArgsConstructor
public final class FogApplicationInputHandler implements VentaEngineInputHandler {
    private final FogApplicationState state;

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
