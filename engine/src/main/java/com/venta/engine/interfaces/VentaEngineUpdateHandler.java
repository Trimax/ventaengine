package com.venta.engine.interfaces;

import com.venta.engine.core.Context;

public interface VentaEngineUpdateHandler {
    VentaEngineUpdateHandler DEFAULT = new VentaEngineUpdateHandler() {};

    default void onUpdate(final double delta, final Context context) {}
}
