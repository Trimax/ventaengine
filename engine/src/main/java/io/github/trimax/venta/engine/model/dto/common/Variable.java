package io.github.trimax.venta.engine.model.dto.common;

import lombok.NonNull;

public record Variable<T>(@NonNull T value, @NonNull T deviation) {
}
