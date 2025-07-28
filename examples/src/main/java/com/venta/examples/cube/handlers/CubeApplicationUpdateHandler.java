package com.venta.examples.cube.handlers;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;

import org.joml.Vector3f;

import com.venta.engine.core.Context;
import com.venta.examples.cube.state.CubeApplicationState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class CubeApplicationUpdateHandler {
    private final CubeApplicationState state;

    private double elapsedTime = 0.f;

    public void onUpdate(final double delta, final Context context) {
        state.getCube().rotate(state.getCubeRotationVelocity());
        handleCubeRotation();

        state.getLight().setPosition(
                new Vector3f(2.5f * (float) Math.sin(elapsedTime), 2.5f, 2.5f * (float) Math.cos(elapsedTime)));

        state.getGizmo().setPosition(state.getLight().getPosition());

        elapsedTime += delta;
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
