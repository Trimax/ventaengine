package io.github.trimax.venta.engine.model.dto;

import lombok.NonNull;
import org.joml.Vector2i;

public record GridMeshDTO(@NonNull Vector2i size,
                          @NonNull Vector2i segments) {
}
