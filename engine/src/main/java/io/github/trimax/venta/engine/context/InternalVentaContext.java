package io.github.trimax.venta.engine.context;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.core.VentaState;
import io.github.trimax.venta.engine.managers.implementation.WindowManagerImplementation;
import io.github.trimax.venta.engine.model.entities.ConsoleEntity;
import io.github.trimax.venta.engine.model.entities.WindowEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class InternalVentaContext {
    private final VentaState state = new VentaState();
    private final ManagerContext managerContext;
    private final VentaContext context;

    public WindowEntity getWindow() {
        return managerContext.get(WindowManagerImplementation.class).getCurrent();
    }

    public ConsoleEntity getConsole() {
        return getWindow().getConsole();
    }
}
