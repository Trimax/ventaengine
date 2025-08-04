package io.github.trimax.examples.material.color;

import io.github.trimax.examples.material.color.handlers.ColoredCubeApplicationStartupHandler;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.NonNull;

public final class ColoredCubeVentaApplication implements VentaEngineApplication {
    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new ColoredCubeApplicationStartupHandler();
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new ColoredCubeVentaApplication());
    }
}
