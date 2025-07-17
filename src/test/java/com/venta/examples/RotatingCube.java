package com.venta.examples;

import com.venta.engine.configuration.WindowConfiguration;
import com.venta.engine.interfaces.Venta;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class RotatingCube implements Venta {
    @Override
    public WindowConfiguration createWindowConfiguration() {
        return new WindowConfiguration("Rotating cube",1024, 768, false);
    }

    @Override
    public void onStartup(final String[] args) {
        log.info("Rotating cube started");
    }

    @Override
    public void onUpdate(final long delta) {

    }
}
