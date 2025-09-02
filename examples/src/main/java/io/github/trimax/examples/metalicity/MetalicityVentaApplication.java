package io.github.trimax.examples.metalicity;

import io.github.trimax.examples.metalicity.handlers.MetalicityApplicationStartupHandler;
import io.github.trimax.examples.metalicity.handlers.MetalicityApplicationUpdateHandler;
import io.github.trimax.examples.metalicity.state.MetalicityApplicationState;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.enums.AntialiasingSamples;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineConfiguration;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.NonNull;

public final class MetalicityVentaApplication implements VentaEngineApplication {
    private final MetalicityApplicationState state = new MetalicityApplicationState();

    @Override
    public @NonNull VentaEngineConfiguration getConfiguration() {
        return new VentaEngineConfiguration() {
            @Override
            public @NonNull RenderConfiguration getRenderConfiguration() {
                return new RenderConfiguration(false, false, true, true, AntialiasingSamples.X4);
            }
        };
    }

    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new MetalicityApplicationStartupHandler(state);
    }

    @Override
    public @NonNull VentaEngineUpdateHandler getUpdateHandler() {
        return new MetalicityApplicationUpdateHandler(state);
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new MetalicityVentaApplication());
    }
}
