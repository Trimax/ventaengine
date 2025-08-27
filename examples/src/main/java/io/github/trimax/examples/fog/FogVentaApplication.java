package io.github.trimax.examples.fog;

import io.github.trimax.examples.fog.handlers.FogApplicationStartupHandler;
import io.github.trimax.examples.fog.handlers.FogApplicationUpdateHandler;
import io.github.trimax.examples.fog.state.FogApplicationState;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class FogVentaApplication implements VentaEngineApplication {
    private final FogApplicationState state = new FogApplicationState();

    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new FogApplicationStartupHandler(state);
    }

    @Override
    public @NonNull VentaEngineUpdateHandler getUpdateHandler() {
        return new FogApplicationUpdateHandler(state);
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new FogVentaApplication());
    }
}
