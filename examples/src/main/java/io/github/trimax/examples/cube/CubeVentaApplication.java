package io.github.trimax.examples.cube;

import io.github.trimax.examples.cube.handlers.CubeApplicationInputHandler;
import io.github.trimax.examples.cube.handlers.CubeApplicationStartupHandler;
import io.github.trimax.examples.cube.handlers.CubeApplicationUpdateHandler;
import io.github.trimax.examples.cube.state.CubeApplicationState;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineInputHandler;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.NonNull;

public final class CubeVentaApplication implements VentaEngineApplication {
    private final CubeApplicationState state = new CubeApplicationState();

    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new CubeApplicationStartupHandler(state);
    }

    @Override
    public @NonNull VentaEngineInputHandler getInputHandler() {
        return new CubeApplicationInputHandler(state);
    }

    @Override
    public @NonNull VentaEngineUpdateHandler getUpdateHandler() {
        return new CubeApplicationUpdateHandler(state);
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new CubeVentaApplication());
    }
}
