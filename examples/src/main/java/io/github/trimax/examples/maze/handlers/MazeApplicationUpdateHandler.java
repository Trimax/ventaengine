package io.github.trimax.examples.maze.handlers;

import io.github.trimax.examples.maze.state.MazeApplicationState;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.lwjgl.glfw.GLFW.*;

@Slf4j
@RequiredArgsConstructor
public final class MazeApplicationUpdateHandler implements VentaEngineUpdateHandler {
    private final MazeApplicationState state;

    public void onUpdate(final Engine.VentaTime time, final VentaContext context) {
        if (context.isButtonPushed(GLFW_KEY_DOWN))
            state.getCamera().moveForward(-(float) time.getDelta());

        if (context.isButtonPushed(GLFW_KEY_UP))
            state.getCamera().moveForward((float) time.getDelta());

        if (context.isButtonPushed(GLFW_KEY_LEFT))
            state.getCamera().moveRight(-(float) time.getDelta());

        if (context.isButtonPushed(GLFW_KEY_RIGHT))
            state.getCamera().moveRight((float) time.getDelta());

        state.getLight().setPosition(state.getCamera().getPosition());
    }
}