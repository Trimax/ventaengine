package io.github.trimax.venta.engine.context;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.controllers.ConsoleController;
import io.github.trimax.venta.engine.model.entity.ConsoleEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class InternalVentaContext {
    private final VentaState state = new VentaState();
    private final ConsoleController consoleController;
    private final ManagerContext managerContext;
    private final VentaContext context;

    public ConsoleEntity getConsole() {
        return consoleController.get();
    }
}
