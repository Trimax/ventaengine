package io.github.trimax.venta.engine.controllers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.factories.ManagerFactory;
import io.github.trimax.venta.engine.factories.RegistryFactory;
import io.github.trimax.venta.engine.interfaces.VentaEngineApplication;
import io.github.trimax.venta.engine.managers.implementation.AbstractManagerImplementation;
import io.github.trimax.venta.engine.model.entity.AbstractEntity;
import io.github.trimax.venta.engine.model.instance.AbstractInstance;
import io.github.trimax.venta.engine.model.states.EngineState;
import io.github.trimax.venta.engine.registries.implementation.AbstractRegistryImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class EngineController extends AbstractController<EngineState, VentaEngineApplication> {
    private final RegistryFactory registryFactory;
    private final ManagerFactory managerFactory;

    @Override
    protected EngineState create(final VentaEngineApplication application) {
        log.debug("Initializing engine state");

        final var state = new EngineState(application);
        state.setDebugEnabled(application.getConfiguration().getRenderConfiguration().isDebugEnabled());

        return state;
    }

    @Override
    protected void destroy(@NonNull final EngineState object) {
        log.debug("Deinitializing engine state");

        registryFactory.cleanup();
        managerFactory.cleanup();
    }

    public <T extends I, I extends AbstractInstance, M extends AbstractManagerImplementation<T, I>> M getManager(@NonNull final Class<M> managerClass) {
        return managerFactory.get(managerClass);
    }

    public <A, T extends E, E extends AbstractEntity, M extends AbstractRegistryImplementation<T, E, A>> M getRegistry(@NonNull final Class<M> registryClass) {
        return registryFactory.get(registryClass);
    }
}
