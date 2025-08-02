package io.github.trimax.venta.engine.core;

import io.github.trimax.venta.container.annotations.Component;
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

    private final WindowManagerImplementation.WindowAccessor windowAccessor;
    private final WindowManagerImplementation windowManager;

    public VentaState getState() {
        return context.getState();
    }

    public WindowManagerImplementation.WindowEntity getWindow() {
        return windowAccessor.get(windowManager.getCurrent());
    }

    public ConsoleManagerImplementation.ConsoleEntity getConsole() {
        return getWindow().getConsole();
    }
}
