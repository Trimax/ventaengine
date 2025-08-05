package io.github.trimax.venta.engine.repositories.implementation;


import io.github.trimax.venta.container.utils.MeasurementUtil;
import io.github.trimax.venta.engine.model.prefabs.AbstractPrefab;
import io.github.trimax.venta.engine.repositories.AbstractRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public abstract class AbstractRepositoryImplementation<T extends P, P extends AbstractPrefab> implements AbstractRepository<P> {
    private final Map<String, T> prefabs = new HashMap<>();

    @Override
    public final T get(@NonNull final String resourcePath) {
        if (!prefabs.containsKey(resourcePath))
            MeasurementUtil.measure("Prefab `" + resourcePath + "` loading",
                    () -> prefabs.put(resourcePath, load(resourcePath)));

        return prefabs.get(resourcePath);
    }

    protected final T get(@NonNull final String resourceName, @NonNull final Supplier<T> creator) {
        final var id = String.format("runtime-%s", resourceName);
        if (!prefabs.containsKey(id))
            MeasurementUtil.measure("Prefab `" + id + "` creation",
                    () -> prefabs.put(id, creator.get()));

        return prefabs.get(id);
    }

    public final void cleanup() {
        log.info("Cleaning up {}", getClass().getSimpleName());
        prefabs.values().forEach(this::unload);
        prefabs.clear();
    }

    protected abstract T load(@NonNull final String resourcePath);

    protected abstract void unload(@NonNull final T entity);
}