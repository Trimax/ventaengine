package io.github.trimax.examples.light.moveable.handlers;

import io.github.trimax.examples.light.moveable.state.MoveableLightApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.RequiredArgsConstructor;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

@RequiredArgsConstructor
public final class MoveableLightApplicationUpdateHandler implements VentaEngineUpdateHandler {
    private final MoveableLightApplicationState state;
    private final Vector3f velocity = new Vector3f();

    public void onUpdate(final Engine.VentaTime time, final VentaContext context) {
        velocity.set(0);

        if (context.isButtonPushed(GLFW_KEY_LEFT))
            velocity.setComponent(0, -0.05f);

        if (context.isButtonPushed(GLFW_KEY_RIGHT))
            velocity.setComponent(0, 0.05f);

        if (context.isButtonPushed(GLFW_KEY_UP))
            velocity.setComponent(2, -0.05f);

        if (context.isButtonPushed(GLFW_KEY_DOWN))
            velocity.setComponent(2, 0.05f);

        if (context.isButtonPushed(GLFW_KEY_KP_ADD))
            state.getLight().setIntensity(state.getLight().getIntensity() + 0.1f);

        if (context.isButtonPushed(GLFW_KEY_KP_SUBTRACT))
            state.getLight().setIntensity(Math.max(0, state.getLight().getIntensity() - 0.1f));

        final var lightPosition = state.getLight().getPosition();
        state.getLight().setPosition(new Vector3f(lightPosition).add(velocity.x(), 0.f, velocity.z()));
    }
}
