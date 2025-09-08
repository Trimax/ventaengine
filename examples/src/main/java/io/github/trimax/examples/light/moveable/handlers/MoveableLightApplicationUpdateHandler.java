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

    public void onUpdate(final Engine.VentaTime time, final VentaContext context) {
        float dx = 0, dz = 0;

        if (context.isButtonPushed(GLFW_KEY_LEFT))
            dx = -0.05f;

        if (context.isButtonPushed(GLFW_KEY_RIGHT))
            dx = 0.05f;

        if (context.isButtonPushed(GLFW_KEY_UP))
            dz = -0.05f;

        if (context.isButtonPushed(GLFW_KEY_DOWN))
            dz = 0.05f;

        final var lightPosition = state.getLight().getPosition();
        state.getLight().setPosition(new Vector3f(lightPosition).add(dx, 0.f, dz));
    }
}
