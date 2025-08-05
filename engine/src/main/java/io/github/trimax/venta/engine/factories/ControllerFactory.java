package io.github.trimax.venta.engine.factories;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.controllers.AbstractController;

import java.util.List;

@Component
public final class ControllerFactory extends AbstractFactory<AbstractController<?, ?>> {
    private ControllerFactory(final List<AbstractController<?, ?>> controllers) {
        super(controllers, AbstractController::deinitialize);
    }
}
