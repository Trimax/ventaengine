package io.github.trimax.venta.engine.model.common.shared;

import lombok.NonNull;

public record Variable<T>(@NonNull T value, @NonNull T deviation) {
}
