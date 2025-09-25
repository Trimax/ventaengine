package io.github.trimax.venta.engine.model.dto;

import io.github.trimax.venta.engine.model.dto.common.WaveDTO;
import lombok.NonNull;

import java.util.List;

public record WaterSurfaceDTO(@NonNull String gridMesh,
                              @NonNull String material,
                              @NonNull String program,
                              List<WaveDTO> waves) {
}
