package io.github.trimax.venta.engine.model.common.dto;

import lombok.NonNull;

public record Range<T>(@NonNull T min, @NonNull T max) {
}
