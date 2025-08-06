package io.github.trimax.examples.maze.handlers;

import io.github.trimax.examples.maze.state.MazeApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;

@Slf4j
@RequiredArgsConstructor
public final class MazeApplicationUpdateHandler implements VentaEngineUpdateHandler {
    private final MazeApplicationState state;
    private static final Vector3f ZERO = new Vector3f(0.f);

    public void onUpdate(final Engine.VentaTime time, final VentaContext context) {
        if (state.getPushedButtons().contains(GLFW_KEY_DOWN))
            state.getCamera().moveForward(-(float) time.getDelta());
        if (state.getPushedButtons().contains(GLFW_KEY_UP))
            state.getCamera().moveForward((float) time.getDelta());
        if (state.getPushedButtons().contains(GLFW_KEY_LEFT))
            state.getCamera().moveRight(-(float) time.getDelta());
        if (state.getPushedButtons().contains(GLFW_KEY_RIGHT))
            state.getCamera().moveRight((float) time.getDelta());

        state.getLight().setPosition(state.getCamera().getPosition());
    }
}