package io.github.trimax.venta.engine.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.implementation.ConsoleManager;
import io.github.trimax.venta.engine.managers.implementation.WindowManager;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class InternalVentaContext {
    @Getter
    private final VentaContext context;

    private final WindowManager.WindowAccessor windowAccessor;
    private final WindowManager windowManager;

    public VentaState getState() {
        return context.getState();
    }

    public WindowManager.WindowEntity getWindow() {
        return windowAccessor.get(windowManager.getCurrent());
    }

    public ConsoleManager.ConsoleEntity getConsole() {
        return getWindow().getConsole();
    }
}
