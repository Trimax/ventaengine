package io.github.trimax.examples.reflection;

import io.github.trimax.examples.reflection.handlers.ReflectionApplicationStartupHandler;
import io.github.trimax.examples.reflection.handlers.ReflectionApplicationUpdateHandler;
import io.github.trimax.examples.reflection.state.ReflectionApplicationState;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.enums.AntialiasingSamples;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineConfiguration;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ReflectionVentaApplication implements VentaEngineApplication {
    private final ReflectionApplicationState state = new ReflectionApplicationState();

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
        return new ReflectionApplicationStartupHandler(state);
    }

    @Override
    public @NonNull VentaEngineUpdateHandler getUpdateHandler() {
        return new ReflectionApplicationUpdateHandler(state);
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new ReflectionVentaApplication());
    }
}
