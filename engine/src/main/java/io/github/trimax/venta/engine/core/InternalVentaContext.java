package io.github.trimax.venta.engine.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.context.ManagerContext;
import io.github.trimax.venta.engine.managers.implementation.ConsoleManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.WindowManagerImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class InternalVentaContext {
    @Getter
    private final VentaContext context;

    @Getter
    private final ManagerContext managerContext;

    public VentaState getState() {
        return context.getState();
    }

    public WindowManagerImplementation.WindowEntity getWindow() {
        return managerContext.get(WindowManagerImplementation.class).getCurrent();
    }

    public ConsoleManagerImplementation.ConsoleEntity getConsole() {
        return getWindow().getConsole();
    }
}
