package com.venta.engine.interfaces;

import com.venta.engine.configurations.RenderConfiguration;
import com.venta.engine.configurations.WindowConfiguration;
import com.venta.engine.core.Context;

public interface VentaEngineApplication {
    WindowConfiguration createWindowConfiguration();

    RenderConfiguration createRenderConfiguration();

    VentaInputHandler createInputHandler();

    void onStartup(final String[] args, final Context context);

    void onUpdate(final double delta, final Context context);
}
