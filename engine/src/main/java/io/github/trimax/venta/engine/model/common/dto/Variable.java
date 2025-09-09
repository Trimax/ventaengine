package io.github.trimax.venta.engine.model.common.dto;

import lombok.NonNull;

public record Variable<T> (@NonNull T value, @NonNull T deviation) {
}
