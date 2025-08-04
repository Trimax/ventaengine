package io.github.trimax.examples.cube.handlers;

import io.github.trimax.examples.cube.state.CubeApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.RequiredArgsConstructor;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

@RequiredArgsConstructor
public final class CubeApplicationUpdateHandler implements VentaEngineUpdateHandler {
    private final CubeApplicationState state;

    public void onUpdate(final Engine.VentaTime time, final VentaContext context) {
        state.getCube().rotate(state.getCubeRotationVelocity());
        handleCubeRotation();

        state.getLightXZ().setPosition(
                new Vector3f(2.5f * (float) Math.sin(time.getTimeElapsed()), 2.5f, 2.5f * (float) Math.cos(time.getTimeElapsed())));

        state.getLightXY().setPosition(
                new Vector3f(2.5f * (float) Math.cos(time.getTimeElapsed()), 2.5f * (float) Math.sin(time.getTimeElapsed()), 2.5f));

        state.getLightYZ().setPosition(
                new Vector3f(2.5f, 2.5f * (float) Math.cos(time.getTimeElapsed()), 2.5f * (float) Math.sin(time.getTimeElapsed())));
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
