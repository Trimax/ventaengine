package io.github.trimax.venta.engine.factories;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.entity.AbstractEntity;
import io.github.trimax.venta.engine.registries.implementation.AbstractRegistryImplementation;
import io.github.trimax.venta.engine.utils.TransformationUtil;
import lombok.NonNull;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Map;

@Component
public final class RegistryFactory {
    private final Map<Class<?>, AbstractRegistryImplementation<?, ?, ?>> registries;

    private RegistryFactory(final List<AbstractRegistryImplementation<?, ?, ?>> registries) {
        this.registries = TransformationUtil.toMap(registries, AbstractRegistryImplementation::getClass);
    }

    public <T extends E, E extends AbstractEntity, M extends AbstractRegistryImplementation<T, E, ?>> M get(@NonNull final Class<M> registryClass) {
        return registryClass.cast(registries.get(registryClass));
    }

    public void cleanup() {
        StreamEx.ofValues(registries).forEach(AbstractRegistryImplementation::cleanup);
    }
}
