package com.venta.container.general;


import io.github.trimax.venta.container.AbstractVentaApplication;
import io.github.trimax.venta.container.annotations.Component;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ComponentC implements AbstractVentaApplication<Void> {
    private ComponentD componentD;

    @Override
    public void start(final String[] args, final Void argument) {
        log.info("Starting ComponentC");
        assertNotNull(componentD, "ComponentD should be injected into ComponentC");
    }
}
