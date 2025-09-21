package io.github.trimax.examples.billboard.handlers;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class BillboardDemoApplicationStartupHandler implements VentaEngineStartupHandler {
    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        final var billboard = context.getBillboardManager().create("Billboard", context.getBillboardRepository().get("default.json"));
        log.info("Billboard loaded: {}", billboard);
    }
}
