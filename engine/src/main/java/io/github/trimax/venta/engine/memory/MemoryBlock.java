package io.github.trimax.venta.engine.memory;

import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
public class MemoryBlock<D> {
    String id = UUID.randomUUID().toString();

    @NonNull
    D data;

    @NonNull
    String description;
}
