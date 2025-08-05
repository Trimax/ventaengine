package io.github.trimax.venta.engine.factories;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.implementation.AbstractManagerImplementation;

import java.util.List;

@Component
public final class ManagerFactory extends AbstractFactory<AbstractManagerImplementation<?, ?>> {
    private ManagerFactory(final List<AbstractManagerImplementation<?, ?>> managers) {
        super(managers, AbstractManagerImplementation::cleanup);
    }
}
