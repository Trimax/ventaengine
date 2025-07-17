package com.venta.engine.interfaces;

import com.venta.engine.configuration.WindowConfiguration;

public interface Venta {
    WindowConfiguration createWindowConfiguration();

    void onStartup(final String[] args);

    void onUpdate(final long delta);
}
