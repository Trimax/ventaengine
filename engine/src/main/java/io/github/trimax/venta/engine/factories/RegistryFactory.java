package io.github.trimax.venta.engine.factories;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.registries.implementation.AbstractRegistryImplementation;

import java.util.List;

@Component
public final class RegistryFactory extends AbstractFactory<AbstractRegistryImplementation<?, ?, ?>> {
    private RegistryFactory(final List<AbstractRegistryImplementation<?, ?, ?>> registries) {
        super(registries, AbstractRegistryImplementation::cleanup);
    }
}
