package io.github.trimax.venta.engine.core;

import io.github.trimax.venta.container.annotations.Component;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class InternalVentaContext {
    private final VentaContext context;

    public VentaState getState() {
        return context.getState();
    }
}
