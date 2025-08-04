package io.github.trimax.examples.light.point;

import io.github.trimax.examples.light.point.handlers.PointLightApplicationStartupHandler;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.NonNull;

public final class PointLightVentaApplication implements VentaEngineApplication {
    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new PointLightApplicationStartupHandler();
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new PointLightVentaApplication());
    }
}
