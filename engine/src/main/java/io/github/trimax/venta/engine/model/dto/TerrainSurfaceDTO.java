package io.github.trimax.venta.engine.model.dto;

import io.github.trimax.venta.engine.model.dto.terrain.TerrainElevationDTO;
import io.github.trimax.venta.engine.model.dto.terrain.TerrainMaterialDTO;
import lombok.NonNull;

import java.util.List;

public record TerrainSurfaceDTO(@NonNull String gridMesh,
                                @NonNull String program,
                                @NonNull TerrainElevationDTO elevation,
                                List<TerrainMaterialDTO> materials) {
}
