package io.github.trimax.venta.engine.factories;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.repositories.implementation.AbstractRepositoryImplementation;

import java.util.List;

@Component
public final class RepositoryFactory extends AbstractFactory<AbstractRepositoryImplementation<?, ?>> {
    private RepositoryFactory(final List<AbstractRepositoryImplementation<?, ?>> repositories) {
        super(repositories, AbstractRepositoryImplementation::cleanup);
    }
}
