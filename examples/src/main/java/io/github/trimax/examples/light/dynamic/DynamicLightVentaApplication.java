package io.github.trimax.examples.light.dynamic;

import io.github.trimax.examples.light.dynamic.handlers.DynamicLightApplicationInputHandler;
import io.github.trimax.examples.light.dynamic.handlers.DynamicLightApplicationStartupHandler;
import io.github.trimax.examples.light.dynamic.handlers.DynamicLightApplicationUpdateHandler;
import io.github.trimax.examples.light.dynamic.state.DynamicLightApplicationState;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineInputHandler;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.NonNull;

public final class DynamicLightVentaApplication implements VentaEngineApplication {
    private final DynamicLightApplicationState state = new DynamicLightApplicationState();

    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new DynamicLightApplicationStartupHandler(state);
    }

    @Override
    public @NonNull VentaEngineInputHandler getInputHandler() {
        return new DynamicLightApplicationInputHandler(state);
    }

    @Override
    public @NonNull VentaEngineUpdateHandler getUpdateHandler() {
        return new DynamicLightApplicationUpdateHandler(state);
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new DynamicLightVentaApplication());
    }
}
