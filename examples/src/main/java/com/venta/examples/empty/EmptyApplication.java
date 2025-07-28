package com.venta.examples.empty;

import com.venta.engine.VentaEngine;
import com.venta.engine.annotations.Component;
import com.venta.engine.core.Context;
import com.venta.engine.interfaces.VentaEngineApplication;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public final class EmptyApplication implements VentaEngineApplication {
    public static void main(final String[] args) {
        VentaEngine.run(args, new EmptyApplication());
    }

    @Override
    public void onStartup(final String[] args, final Context context) {
        log.info("Empty application started");
    }

    @Override
    public void onUpdate(final double delta, final Context context) {

    }
}
