package io.github.trimax.venta.engine.factories;

import io.github.trimax.venta.engine.utils.TransformationUtil;
import lombok.NonNull;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractFactory<T> {
    private final Map<Class<?>, T> instances;
    private final Consumer<T> cleaner;

    protected AbstractFactory(@NonNull final List<T> instances, @NonNull final Consumer<T> cleaner) {
        this.instances = TransformationUtil.toMap(instances, T::getClass);
        this.cleaner = cleaner;
    }

    public void cleanup() {
        StreamEx.ofValues(instances).forEach(cleaner);
    }

    public <C extends T> C get(@NonNull final Class<C> instanceClass) {
        return instanceClass.cast(instances.get(instanceClass));
    }
}

