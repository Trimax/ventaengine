package io.github.trimax.venta.engine.model.dto;

import org.joml.Vector2i;

import io.github.trimax.venta.engine.model.dto.gridmesh.ElevationDTO;
import lombok.NonNull;

public record GridMeshDTO(@NonNull Vector2i size,
                          @NonNull Vector2i segments,
                          ElevationDTO elevation) {
}
