package com.venta.container.injection;

import io.github.trimax.venta.container.AbstractVentaApplication;
import io.github.trimax.venta.container.annotations.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public final class NoInjectConstructorComponent implements AbstractVentaApplication<Void> {
    public NoInjectConstructorComponent(String arg) {}
    public NoInjectConstructorComponent(int arg, String anotherArg) {}

    @Override
    public void start(final String[] args, final Void argument) {
        log.info("Hello from NoInjectConstructorComponent!");
    }
}
