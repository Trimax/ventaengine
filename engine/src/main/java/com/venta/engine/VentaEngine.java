package com.venta.engine;

import com.venta.container.annotations.Component;
import com.venta.container.AbstractVentaApplication;
import com.venta.container.VentaApplication;
import com.venta.engine.core.VentaContext;
import com.venta.engine.core.Engine;
import com.venta.engine.interfaces.VentaEngineApplication;
import com.venta.engine.utils.ResourceUtil;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public final class VentaEngine implements AbstractVentaApplication<VentaEngineApplication> {
    private final Engine engine;

    public static void run(final String[] args, @NonNull final VentaEngineApplication application) {
        log.info("Starting VentaEngine {}", ResourceUtil.load("/banner.txt"));
        VentaApplication.run(args, VentaEngine.class, application);
    }

    @Override
    public void start(final String[] args, @NonNull final VentaEngineApplication venta) {
        engine.initialize(venta);
        venta.getStartupHandler().onStartup(args, VentaApplication.getComponent(VentaContext.class));
        engine.run();
    }
}
