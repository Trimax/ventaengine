package io.github.trimax.venta.engine.model.dto.object;

import io.github.trimax.venta.engine.model.common.math.Transform;
import lombok.NonNull;

public record ObjectMeshDTO(@NonNull String mesh,
                            @NonNull Transform transform,
                            String material) {
}