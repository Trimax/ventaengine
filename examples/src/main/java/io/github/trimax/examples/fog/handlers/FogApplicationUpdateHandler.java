package io.github.trimax.examples.fog.handlers;

import io.github.trimax.examples.fog.state.FogApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

import static io.github.trimax.examples.fog.definitions.Definitions.*;
import static org.lwjgl.glfw.GLFW.*;

@Slf4j
@RequiredArgsConstructor
public final class FogApplicationUpdateHandler implements VentaEngineUpdateHandler {
    private final FogApplicationState state;
    private static float rotationSpeed = 30.f;
    private float rotationAngle = 0.f;
    private float fogDistance = 5.f;

    public void onUpdate(final Engine.VentaTime time, final VentaContext context) {
        final float delta = (float) time.getDelta();

        if (context.isButtonPushed(GLFW_KEY_DOWN))
            state.getCamera().moveForward(-delta);
        if (context.isButtonPushed(GLFW_KEY_UP))
            state.getCamera().moveForward(delta);
        if (context.isButtonPushed(GLFW_KEY_LEFT))
            state.getCamera().moveRight(-delta);
        if (context.isButtonPushed(GLFW_KEY_RIGHT))
            state.getCamera().moveRight(delta);

        if (context.isButtonPushed(GLFW_KEY_KP_ADD))
            rotationSpeed += 1;
        if (context.isButtonPushed(GLFW_KEY_KP_SUBTRACT))
            rotationSpeed = Math.max(0, rotationSpeed - 1);

        if (context.isButtonPushed(GLFW_KEY_W))
            fogDistance += 1;
        if (context.isButtonPushed(GLFW_KEY_S))
            fogDistance = Math.max(0, fogDistance - 1);

        state.getFog().setMinimalDistance(fogDistance);
        state.getFog().setMaximalDistance(fogDistance + FOG_SPACE);

        rotationAngle += rotationSpeed * delta;
        if (rotationAngle >= 360.0f)
            rotationAngle -= 360.0f;

        updateCubesPosition();
    }

    private void updateCubesPosition() {
        for (int i = 0; i < state.getCubes().size(); i++) {
            final float radians = (float) Math.toRadians(i * (360.0f / CUBE_COUNT) + rotationAngle);

            final var cube = state.getCubes().get(i);
            cube.setRotation(new Vector3f(0.f, -radians, 0.f));
            cube.setPosition(new Vector3f((float) Math.cos(radians),0.0f, (float) Math.sin(radians)).mul(CIRCLE_RADIUS));
        }
    }
}