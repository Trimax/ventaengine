package com.venta.engine.interfaces;

import com.venta.engine.core.VentaContext;
import com.venta.engine.core.Engine;

public interface VentaEngineUpdateHandler {
    VentaEngineUpdateHandler DEFAULT = new VentaEngineUpdateHandler() {};

    default void onUpdate(final Engine.VentaTime time, final VentaContext context) {}
}
