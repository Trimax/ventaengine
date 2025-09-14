package io.github.trimax.examples.gridmesh.water;

import io.github.trimax.examples.gridmesh.water.handlers.WaterApplicationStartupHandler;
import io.github.trimax.examples.gridmesh.water.handlers.WaterApplicationUpdateHandler;
import io.github.trimax.examples.gridmesh.water.state.WaterApplicationState;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.enums.AntialiasingSamples;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineConfiguration;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class WaterVentaApplication implements VentaEngineApplication {
    private final WaterApplicationState state = new WaterApplicationState();

    @Override
    public @NonNull VentaEngineConfiguration getConfiguration() {
        return new VentaEngineConfiguration() {
            @Override
            public @NonNull RenderConfiguration getRenderConfiguration() {
                return new RenderConfiguration(false, false, true, true, AntialiasingSamples.X8);
            }
        };
    }

    @Override
    public @NonNull VentaEngineUpdateHandler getUpdateHandler() {
        return new WaterApplicationUpdateHandler(state);
    }

    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new WaterApplicationStartupHandler(state);
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new WaterVentaApplication());
    }
}
