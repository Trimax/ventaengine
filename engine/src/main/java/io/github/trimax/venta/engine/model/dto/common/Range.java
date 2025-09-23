package io.github.trimax.venta.engine.model.dto.common;

import lombok.NonNull;

public record Range<T>(@NonNull T min, @NonNull T max) {
}
