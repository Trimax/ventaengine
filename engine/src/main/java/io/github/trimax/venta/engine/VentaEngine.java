package io.github.trimax.venta.engine;

import io.github.trimax.venta.container.AbstractVentaApplication;
import io.github.trimax.venta.container.VentaApplication;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.core.Engine;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.utils.ResourceUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class VentaEngine implements AbstractVentaApplication<VentaEngineApplication> {
    private final VentaContext context;
    private final Engine engine;

    public static void run(final String[] args, @NonNull final VentaEngineApplication application) {
        log.info("Starting {}", ResourceUtil.loadAsString("/banner.txt"));
        VentaApplication.run(args, VentaEngine.class, application);
    }

    @Override
    public void start(final String[] args, @NonNull final VentaEngineApplication venta) {
        engine.initialize(venta);
        venta.getStartupHandler().onStartup(args, context);

        engine.run();
    }
}
