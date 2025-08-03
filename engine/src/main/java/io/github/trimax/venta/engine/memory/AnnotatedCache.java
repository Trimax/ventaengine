package io.github.trimax.venta.engine.memory;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@AllArgsConstructor
public final class AnnotatedCache<T> {
    private final Map<T, String> resources = new HashMap<>();

    @NonNull
    private final Supplier<T> creator;

    @NonNull
    private final Consumer<T> deleter;

    public T create(@NonNull final String format, final Object... arguments) {
        final var object = this.creator.get();
        resources.put(object, String.format(format, arguments));

        return object;
    }

    public void delete(@NonNull final T resource) {
        this.deleter.accept(resource);
        resources.remove(resource);
    }

    void cleanup() {
        for (final var resource : resources.entrySet()) {
            deleter.accept(resource.getKey());
            log.warn("Resource {} was not deleted {}", resource.getKey(), resource.getValue());
        }

        resources.clear();
    }
}
