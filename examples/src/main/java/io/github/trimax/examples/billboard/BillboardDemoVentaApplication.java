package io.github.trimax.examples.billboard;

import io.github.trimax.examples.billboard.handlers.BillboardDemoApplicationStartupHandler;
import io.github.trimax.venta.engine.VentaEngine;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.NonNull;

public final class BillboardDemoVentaApplication implements VentaEngineApplication {
    @Override
    public @NonNull VentaEngineStartupHandler getStartupHandler() {
        return new BillboardDemoApplicationStartupHandler();
    }

    public static void main(final String[] args) {
        VentaEngine.run(args, new BillboardDemoVentaApplication());
    }
}
