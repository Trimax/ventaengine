package io.github.trimax.examples.billboard;

import io.github.trimax.examples.billboard.handlers.BillboardApplicationStartupHandler;
import io.github.trimax.examples.billboard.handlers.BillboardApplicationUpdateHandler;
import io.github.trimax.examples.billboard.state.BillboardApplicationState;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.enums.AntialiasingSamples;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineConfiguration;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.NonNull;

public final class BillboardVentaApplication implements VentaEngineApplication {
    private final BillboardApplicationState state = new BillboardApplicationState();

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
        return new BillboardApplicationStartupHandler(state);
    }

    @Override
    public @NonNull VentaEngineUpdateHandler getUpdateHandler() {
        return new BillboardApplicationUpdateHandler(state);
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new BillboardVentaApplication());
    }
}
