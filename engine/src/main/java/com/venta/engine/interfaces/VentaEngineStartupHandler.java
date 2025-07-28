package com.venta.engine.interfaces;

import com.venta.engine.core.Context;

public interface VentaEngineStartupHandler {
    VentaEngineStartupHandler DEFAULT = new VentaEngineStartupHandler() {};

    default void onStartup(final String[] args, final Context context) {}
}
