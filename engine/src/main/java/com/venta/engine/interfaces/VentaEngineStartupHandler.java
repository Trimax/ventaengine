package com.venta.engine.interfaces;

import com.venta.engine.core.VentaContext;

public interface VentaEngineStartupHandler {
    VentaEngineStartupHandler DEFAULT = new VentaEngineStartupHandler() {};

    default void onStartup(final String[] args, final VentaContext context) {}
}
