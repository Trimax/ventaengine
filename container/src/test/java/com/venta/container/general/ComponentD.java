package com.venta.container.general;

import io.github.trimax.venta.container.AbstractVentaApplication;
import io.github.trimax.venta.container.annotations.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public final class ComponentD implements AbstractVentaApplication<Void> {

    @Override
    public void start(final String[] args, final Void argument) {
        log.info("Starting ComponentD");
    }
}
