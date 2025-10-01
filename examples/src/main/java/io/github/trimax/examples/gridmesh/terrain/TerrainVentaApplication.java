package io.github.trimax.examples.gridmesh.terrain;

import io.github.trimax.examples.gridmesh.terrain.handlers.TerrainApplicationStartupHandler;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.enums.AntialiasingSamples;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineConfiguration;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class TerrainVentaApplication implements VentaEngineApplication {
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
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new TerrainApplicationStartupHandler();
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new TerrainVentaApplication());
    }
}
