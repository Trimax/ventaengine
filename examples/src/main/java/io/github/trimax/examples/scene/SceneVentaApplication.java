package io.github.trimax.examples.scene;

import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.NonNull;

public final class SceneVentaApplication implements VentaEngineApplication {
    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new SceneApplicationStartupHandler();
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new SceneVentaApplication());
    }
}
