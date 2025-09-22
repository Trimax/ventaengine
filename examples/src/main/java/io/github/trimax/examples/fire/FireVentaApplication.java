package io.github.trimax.examples.fire;

import io.github.trimax.examples.fire.handlers.FireApplicationStartupHandler;
import io.github.trimax.examples.fire.handlers.FireApplicationUpdateHandler;
import io.github.trimax.examples.fire.state.FireApplicationState;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.enums.AntialiasingSamples;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineConfiguration;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.NonNull;

public final class FireVentaApplication implements VentaEngineApplication {
    private final FireApplicationState state = new FireApplicationState();

    @Override
    public @NonNull VentaEngineConfiguration getConfiguration() {
        return new VentaEngineConfiguration() {
            @Override
            public @NonNull RenderConfiguration getRenderConfiguration() {
                return new RenderConfiguration(true, true, true, true, AntialiasingSamples.X2);
            }
        };
    }

    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new FireApplicationStartupHandler(state);
    }

    @Override
    public @NonNull VentaEngineUpdateHandler getUpdateHandler() {
        return new FireApplicationUpdateHandler(state);
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new FireVentaApplication());
    }
}
