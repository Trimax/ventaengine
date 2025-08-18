package io.github.trimax.examples.light.dynamic.handlers;

import io.github.trimax.examples.light.dynamic.state.DynamicLightApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.RequiredArgsConstructor;
import org.joml.Vector3f;

import java.util.function.Predicate;

import static org.lwjgl.glfw.GLFW.*;

@RequiredArgsConstructor
public final class DynamicLightApplicationUpdateHandler implements VentaEngineUpdateHandler {
    private final DynamicLightApplicationState state;

    public void onUpdate(final Engine.VentaTime time, final VentaContext context) {
        state.getCube().rotate(state.getCubeRotationVelocity());
        handleCubeRotation(context::isButtonPushed);

        state.getLightXZ().setPosition(
                new Vector3f(2.5f * (float) Math.sin(time.getTimeElapsed()), 2.5f, 2.5f * (float) Math.cos(time.getTimeElapsed())));

        state.getLightXY().setPosition(
                new Vector3f(2.5f * (float) Math.cos(time.getTimeElapsed()), 2.5f * (float) Math.sin(time.getTimeElapsed()), 2.5f));

        state.getLightYZ().setPosition(
                new Vector3f(2.5f, 2.5f * (float) Math.cos(time.getTimeElapsed()), 2.5f * (float) Math.sin(time.getTimeElapsed())));
    }

    private void handleCubeRotation(final Predicate<Integer> isButtonPushed) {
        state.getCubeRotationVelocity().set(0.f);

        if (isButtonPushed.test(GLFW_KEY_LEFT))
            state.getCubeRotationVelocity().y = -0.05f;

        if (isButtonPushed.test(GLFW_KEY_RIGHT))
            state.getCubeRotationVelocity().y = 0.05f;

        if (isButtonPushed.test(GLFW_KEY_UP))
            state.getCubeRotationVelocity().x = -0.05f;

        if (isButtonPushed.test(GLFW_KEY_DOWN))
            state.getCubeRotationVelocity().x = 0.05f;
    }
}
