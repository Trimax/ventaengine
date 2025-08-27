package io.github.trimax.examples.maze;

import io.github.trimax.examples.maze.handlers.MazeApplicationInputHandler;
import io.github.trimax.examples.maze.handlers.MazeApplicationStartupHandler;
import io.github.trimax.examples.maze.handlers.MazeApplicationUpdateHandler;
import io.github.trimax.examples.maze.state.MazeApplicationState;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.enums.AntialiasingSamples;
import io.github.trimax.venta.engine.interfaces.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class MazeVentaApplication implements VentaEngineApplication {
    private final MazeApplicationState state = new MazeApplicationState();

    @Override
    public @NonNull VentaEngineConfiguration getConfiguration() {
        return new VentaEngineConfiguration() {
            @Override
            public @NonNull RenderConfiguration getRenderConfiguration() {
                return new RenderConfiguration(false, false, false, true, AntialiasingSamples.X8);
            }
        };
    }

    @Override
    public @NonNull VentaEngineInputHandler getInputHandler() {
        return new MazeApplicationInputHandler(state);
    }

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
