package io.github.trimax.venta.engine.model.dto;

import org.joml.Vector2i;

import lombok.NonNull;

public record GridMeshDTO(@NonNull Vector2i size,
                          @NonNull Vector2i segments,
                          String program,
                          String heightmap) {
}
