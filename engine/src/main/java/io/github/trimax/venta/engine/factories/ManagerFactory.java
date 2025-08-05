package io.github.trimax.venta.engine.factories;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.implementation.AbstractManagerImplementation;
import io.github.trimax.venta.engine.model.instance.AbstractInstance;
import io.github.trimax.venta.engine.utils.TransformationUtil;
import lombok.NonNull;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Map;

@Component
public class ManagerFactory extends AbstractFactory {
    private final Map<Class<?>, AbstractManagerImplementation<?, ?>> managers;

    private ManagerFactory(final List<AbstractManagerImplementation<?, ?>> managers) {
        this.managers = TransformationUtil.toMap(managers, AbstractManagerImplementation::getClass);
    }

    public <T extends I, I extends AbstractInstance, M extends AbstractManagerImplementation<T, I>> M get(@NonNull final Class<M> managerClass) {
        return managerClass.cast(managers.get(managerClass));
    }

    public void cleanup() {
        StreamEx.ofValues(managers).forEach(AbstractManagerImplementation::cleanup);
    }
}
