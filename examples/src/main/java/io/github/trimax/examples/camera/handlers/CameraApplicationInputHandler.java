package io.github.trimax.examples.camera.handlers;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import io.github.trimax.examples.camera.state.CameraApplicationState;
import io.github.trimax.venta.engine.interfaces.VentaEngineInputHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public final class CameraApplicationInputHandler implements VentaEngineInputHandler {
    private final CameraApplicationState state;

    @Override
    public void onKey(final int key, final int scancode, final int action, final int mods) {
        if (action == GLFW_PRESS)
            state.getPushedButtons().add(key);

        if (action == GLFW_RELEASE)
            state.getPushedButtons().remove(key);
    }
}
