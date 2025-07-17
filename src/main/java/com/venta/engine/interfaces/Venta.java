package com.venta.engine.interfaces;

import com.venta.engine.configuration.WindowConfiguration;
import com.venta.engine.core.Context;

public interface Venta {
    WindowConfiguration createWindowConfiguration();

    void onStartup(final String[] args, final Context context);

    void onUpdate(final long delta, final Context context);
}
