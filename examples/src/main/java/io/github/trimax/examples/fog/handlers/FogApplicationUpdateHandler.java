package io.github.trimax.examples.fog.handlers;

import io.github.trimax.examples.fog.state.FogApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

@Slf4j
@RequiredArgsConstructor
public final class FogApplicationUpdateHandler implements VentaEngineUpdateHandler {
    private final FogApplicationState state;
    private static final int CIRCLE_RADIUS = 10;
    private static final int CUBE_COUNT = 10;
    private static final float ROTATION_SPEED = 30.0f;
    private float rotationAngle = 0.0f;

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

        final float rotationDelta = ROTATION_SPEED * delta;
        rotationAngle += rotationDelta;

        if (rotationAngle >= 360.0f)
            rotationAngle -= 360.0f;

        for (int i = 0; i < state.getCubes().size(); i++) {
            final var cube = state.getCubes().get(i);
            final float baseAngle = i * (360.0f / CUBE_COUNT);
            final float currentAngle = baseAngle + rotationAngle;
            final float radians = (float) Math.toRadians(currentAngle);

            cube.setPosition(new Vector3f(
                    CIRCLE_RADIUS * (float) Math.cos(radians),
                    0.0f,
                    CIRCLE_RADIUS * (float) Math.sin(radians)
            ));
        }
    }
}