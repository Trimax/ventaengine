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
    private final Map<String, MemoryBlock<T>> resources = new HashMap<>();

    @NonNull
    private final Supplier<T> creator;

    @NonNull
    private final Consumer<T> deleter;

    public MemoryBlock<T> create(@NonNull final String format, final Object... arguments) {
        return create(this.creator, format, arguments);
    }

    public MemoryBlock<T> create(@NonNull final Supplier<T> creator, @NonNull final String format, final Object... arguments) {
        final var object = new MemoryBlock<>(creator.get(), String.format(format, arguments));
        this.resources.put(object.getId(), object);

        return object;
    }

    public void delete(@NonNull final MemoryBlock<T> resource) {
        if (this.resources.remove(resource.getId()) != null)
            this.deleter.accept(resource.getData());
    }

    void cleanup() {
        for (final var resource : resources.entrySet()) {
            log.warn("Resource {} was not deleted {}", resource.getKey(), resource.getValue());
            deleter.accept(resource.getValue().getData());
        }

        this.resources.clear();
    }
}
