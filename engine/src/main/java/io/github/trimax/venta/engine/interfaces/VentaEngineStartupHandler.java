package io.github.trimax.venta.engine.interfaces;

import io.github.trimax.venta.engine.context.VentaContext;

public interface VentaEngineStartupHandler {
    VentaEngineStartupHandler DEFAULT = new VentaEngineStartupHandler() {};

    default void onStartup(final String[] args, final VentaContext context) {}
}
