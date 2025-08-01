package com.venta.container.cyclic;

import io.github.trimax.venta.container.AbstractVentaApplication;
import io.github.trimax.venta.container.annotations.Component;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ComponentB implements AbstractVentaApplication<Void> {
    private ComponentA componentA;

    @Override
    public void start(final String[] args, final Void argument) {
        log.info("Hello from ComponentB!");
    }
}
