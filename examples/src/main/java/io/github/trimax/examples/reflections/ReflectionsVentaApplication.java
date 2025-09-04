package io.github.trimax.examples.reflections;

import io.github.trimax.examples.reflections.handlers.ReflectionsApplicationStartupHandler;
import io.github.trimax.examples.reflections.handlers.ReflectionsApplicationUpdateHandler;
import io.github.trimax.examples.reflections.state.ReflectionsApplicationState;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.enums.AntialiasingSamples;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineConfiguration;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ReflectionsVentaApplication implements VentaEngineApplication {
    private final ReflectionsApplicationState state = new ReflectionsApplicationState();

    @Override
    public @NonNull VentaEngineConfiguration getConfiguration() {
        return new VentaEngineConfiguration() {
            @Override
            public @NonNull RenderConfiguration getRenderConfiguration() {
                return new RenderConfiguration(false, true, true, true, AntialiasingSamples.X8);
            }
        };
    }

    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new ReflectionsApplicationStartupHandler(state);
    }

    @Override
    public @NonNull VentaEngineUpdateHandler getUpdateHandler() {
        return new ReflectionsApplicationUpdateHandler(state);
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new ReflectionsVentaApplication());
    }
}
