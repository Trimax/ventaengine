package io.github.trimax.examples.cube.coloured.material;

import io.github.trimax.examples.cube.coloured.material.handlers.ColouredCubeApplicationStartupHandler;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ColouredCubeVentaApplication implements VentaEngineApplication {

    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new ColouredCubeApplicationStartupHandler();
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new ColouredCubeVentaApplication());
    }
}
