package io.github.trimax.examples.plane.light;

import io.github.trimax.examples.plane.light.handlers.PlaneLightApplicationStartupHandler;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.NonNull;

public final class PlaneLightVentaApplication implements VentaEngineApplication {
    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new PlaneLightApplicationStartupHandler();
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new PlaneLightVentaApplication());
    }
}
