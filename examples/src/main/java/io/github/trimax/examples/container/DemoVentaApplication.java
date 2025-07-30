package io.github.trimax.examples.container;

import io.github.trimax.venta.container.AbstractVentaApplication;
import io.github.trimax.venta.container.VentaApplication;
import io.github.trimax.venta.container.annotations.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public final class DemoVentaApplication implements AbstractVentaApplication<Void> {
    @Override
    public void start(final String[] args, final Void argument) {
        log.info("Hello world from Venta!");
    }

    public static void main(final String[] args) {
        VentaApplication.run(args, DemoVentaApplication.class);
    }
}