package io.github.trimax.examples.maze;

import io.github.trimax.examples.maze.handlers.MazeApplicationStartupHandler;
import io.github.trimax.examples.maze.handlers.MazeApplicationUpdateHandler;
import io.github.trimax.examples.maze.state.MazeApplicationState;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class MazeVentaApplication implements VentaEngineApplication {
    private final MazeApplicationState state = new MazeApplicationState();

    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new MazeApplicationStartupHandler(state);
    }

    @Override
    public @NonNull VentaEngineUpdateHandler getUpdateHandler() {
        return new MazeApplicationUpdateHandler(state);
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new MazeVentaApplication());
    }
}
