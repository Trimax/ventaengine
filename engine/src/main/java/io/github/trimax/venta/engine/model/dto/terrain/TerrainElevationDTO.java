package io.github.trimax.venta.engine.model.dto.terrain;

import lombok.NonNull;

public record TerrainElevationDTO(@NonNull String heightmap, float factor) {
}
