package io.github.trimax.venta.engine.model.common.wrap;

import io.github.trimax.venta.engine.model.common.math.Transform;
import lombok.NonNull;

public record Transformed<T>(@NonNull T value, @NonNull Transform transform) {
}
