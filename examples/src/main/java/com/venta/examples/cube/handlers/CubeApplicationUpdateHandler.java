package com.venta.examples.cube.handlers;

import static org.lwjgl.glfw.GLFW.*;

import org.joml.Vector3f;

import com.venta.engine.core.Engine;
import com.venta.engine.core.VentaContext;
import com.venta.engine.interfaces.VentaEngineUpdateHandler;
import com.venta.examples.cube.state.CubeApplicationState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class CubeApplicationUpdateHandler implements VentaEngineUpdateHandler {
    private final CubeApplicationState state;

    public void onUpdate(final Engine.VentaTime time, final VentaContext context) {
        state.getCube().rotate(state.getCubeRotationVelocity());
        handleCubeRotation();

        state.getLight().setPosition(
                new Vector3f(2.5f * (float) Math.sin(time.getTimeElapsed()), 2.5f, 2.5f * (float) Math.cos(time.getTimeElapsed())));
    }

    private void handleCubeRotation() {
        state.getCubeRotationVelocity().set(0.f);
        if (state.getPushedButtons().contains(GLFW_KEY_LEFT))
            state.getCubeRotationVelocity().y = -0.05f;

        if (state.getPushedButtons().contains(GLFW_KEY_RIGHT))
            state.getCubeRotationVelocity().y = 0.05f;

        if (state.getPushedButtons().contains(GLFW_KEY_UP))
            state.getCubeRotationVelocity().x = -0.05f;

        if (state.getPushedButtons().contains(GLFW_KEY_DOWN))
            state.getCubeRotationVelocity().x = 0.05f;
    }
}
