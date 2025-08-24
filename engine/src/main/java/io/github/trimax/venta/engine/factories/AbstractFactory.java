package io.github.trimax.venta.engine.factories;

import io.github.trimax.venta.engine.utils.TransformationUtil;
import lombok.NonNull;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractFactory<T> {
    private final Map<Class<?>, T> instances;
    private final Consumer<T> cleaner;

    protected AbstractFactory(@NonNull final List<T> instances, @NonNull final Consumer<T> cleaner) {
        this.instances = TransformationUtil.toMap(instances, T::getClass);
        this.cleaner = cleaner;
    }

    protected final T getBy(final Predicate<T> predicate) {
        return StreamEx.ofValues(instances)
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    protected final <V> T getBy(final Function<T, V> mapper, final V value) {
        return StreamEx.ofValues(instances)
                .filterBy(mapper, value)
                .findFirst()
                .orElse(null);
    }

    public void cleanup() {
        StreamEx.ofValues(instances).forEach(cleaner);
    }

    public <C extends T> C get(@NonNull final Class<C> instanceClass) {
        return instanceClass.cast(instances.get(instanceClass));
    }
}

