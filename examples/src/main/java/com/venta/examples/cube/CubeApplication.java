package com.venta.examples.cube;

import com.venta.engine.VentaEngine;
import com.venta.engine.interfaces.VentaEngineApplication;
import com.venta.engine.interfaces.VentaEngineInputHandler;
import com.venta.engine.interfaces.VentaEngineStartupHandler;
import com.venta.engine.interfaces.VentaEngineUpdateHandler;
import com.venta.examples.cube.handlers.CubeApplicationInputHandler;
import com.venta.examples.cube.handlers.CubeApplicationStartupHandler;
import com.venta.examples.cube.handlers.CubeApplicationUpdateHandler;
import com.venta.examples.cube.state.CubeApplicationState;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class CubeApplication implements VentaEngineApplication {
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
        VentaEngine.run(args, new CubeApplication());
    }
}
