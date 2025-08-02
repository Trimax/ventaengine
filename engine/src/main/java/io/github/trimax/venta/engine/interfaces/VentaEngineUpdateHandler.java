package io.github.trimax.venta.engine.interfaces;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.core.Engine;

public interface VentaEngineUpdateHandler {
    VentaEngineUpdateHandler DEFAULT = new VentaEngineUpdateHandler() {};

    default void onUpdate(final Engine.VentaTime time, final VentaContext context) {}
}
