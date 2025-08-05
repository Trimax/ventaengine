package io.github.trimax.venta.engine.factories;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.controllers.AbstractController;
import io.github.trimax.venta.engine.model.states.AbstractState;
import io.github.trimax.venta.engine.utils.TransformationUtil;
import lombok.NonNull;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Map;

@Component
public final class ControllerFactory {
    private final Map<Class<?>, AbstractController<?, ?>> controllers;

    private ControllerFactory(final List<AbstractController<?, ?>> controllers) {
        this.controllers = TransformationUtil.toMap(controllers, AbstractController::getClass);
    }

    public <A, S extends AbstractState, C extends AbstractController<S, A>> C get(@NonNull final Class<C> controllerClass) {
        return controllerClass.cast(controllers.get(controllerClass));
    }

    public void cleanup() {
        StreamEx.ofValues(controllers).forEach(AbstractController::deinitialize);
    }
}
