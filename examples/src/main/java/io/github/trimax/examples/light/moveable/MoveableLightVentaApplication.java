package io.github.trimax.examples.light.moveable;

import io.github.trimax.examples.light.moveable.handlers.MoveableLightApplicationStartupHandler;
import io.github.trimax.examples.light.moveable.handlers.MoveableLightApplicationUpdateHandler;
import io.github.trimax.examples.light.moveable.state.MoveableLightApplicationState;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import io.github.trimax.venta.engine.interfaces.VentaEngineUpdateHandler;
import lombok.NonNull;

public final class MoveableLightVentaApplication implements VentaEngineApplication {
    private final MoveableLightApplicationState state = new MoveableLightApplicationState();

    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new MoveableLightApplicationStartupHandler(state);
    }

    @Override
    public @NonNull VentaEngineUpdateHandler getUpdateHandler() {
        return new MoveableLightApplicationUpdateHandler(state);
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new MoveableLightVentaApplication());
    }
}
