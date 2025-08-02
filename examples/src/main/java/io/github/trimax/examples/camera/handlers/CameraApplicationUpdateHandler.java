package io.github.trimax.examples.camera.handlers;

import io.github.trimax.examples.camera.state.CameraApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;

@Slf4j
@RequiredArgsConstructor
public final class CameraApplicationUpdateHandler implements VentaEngineUpdateHandler {
    private static final Vector3f ZERO = new Vector3f(0.f);
    private final CameraApplicationState state;

    public void onUpdate(final Engine.VentaTime time, final VentaContext context) {
        if (state.getPushedButtons().contains(GLFW_KEY_LEFT))
            state.setCameraAngle(state.getCameraAngle() + (float) time.getDelta());

        if (state.getPushedButtons().contains(GLFW_KEY_RIGHT))
            state.setCameraAngle(state.getCameraAngle() - (float) time.getDelta());

        state.getCamera().setPosition(new Vector3f(2.5f * (float) Math.sin(state.getCameraAngle()), 2.5f, 2.5f * (float) Math.cos(state.getCameraAngle())));
        state.getCamera().lookAt(ZERO);
    }
}
