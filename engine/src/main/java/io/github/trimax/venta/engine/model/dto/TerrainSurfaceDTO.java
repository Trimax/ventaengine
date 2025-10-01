package io.github.trimax.venta.engine.model.dto;

import lombok.NonNull;

public record TerrainSurfaceDTO(@NonNull String gridMesh,
                                @NonNull String material,
                                @NonNull String program) {
}
