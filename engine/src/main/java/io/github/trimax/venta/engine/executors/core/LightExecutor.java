package io.github.trimax.venta.engine.executors.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.executors.light.AbstractLightExecutor;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class LightExecutor extends AbstractCoreExecutor {
    private LightExecutor(@NonNull final ControllerFactory factory, @NonNull final List<AbstractLightExecutor> executors) {
        super(factory, "light", "the set of commands to manage lights", executors);
    }
}
