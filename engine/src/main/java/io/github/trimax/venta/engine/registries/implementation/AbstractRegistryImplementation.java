package io.github.trimax.venta.engine.registries.implementation;

import io.github.trimax.venta.container.utils.MeasurementUtil;
import io.github.trimax.venta.engine.model.entity.AbstractEntity;
import io.github.trimax.venta.engine.registries.AbstractRegistry;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


@Slf4j
public abstract class AbstractRegistryImplementation<T extends E, E extends AbstractEntity, A> implements AbstractRegistry<E, A> {
    private final Map<String, T> entities = new HashMap<>();

    @Override
    public final T get(@NonNull final String resourcePath, final A argument) {
        if (!entities.containsKey(resourcePath))
            MeasurementUtil.measure("Entity `" + resourcePath + "` loading",
                    () -> entities.put(resourcePath, load(resourcePath, argument)));

        return entities.get(resourcePath);
    }

    @Override
    public final T get(@NonNull final String resourcePath) {
        return get(resourcePath, (A) null);
    }

    protected final T get(@NonNull final String resourceName, @NonNull final Supplier<T> creator) {
        final var id = String.format("runtime-%s", resourceName);
        if (!entities.containsKey(id))
            MeasurementUtil.measure("Entity `" + id + "` creation",
                    () -> entities.put(id, creator.get()));

        return entities.get(id);
    }

    public final void cleanup() {
        log.info("Cleaning up {}", getClass().getSimpleName());
        entities.values().forEach(this::unload);
        entities.clear();
    }

    protected abstract T load(@NonNull final String resourcePath, final A argument);

    protected abstract void unload(@NonNull final T prefab);
}