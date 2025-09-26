package io.github.trimax.venta.engine.model.dto;

import java.util.List;

import io.github.trimax.venta.engine.model.dto.common.NoiseDTO;
import io.github.trimax.venta.engine.model.dto.common.WaveDTO;
import lombok.NonNull;

public record WaterSurfaceDTO(@NonNull String gridMesh,
                              @NonNull String material,
                              @NonNull String program,
                              List<NoiseDTO> noises,
                              List<WaveDTO> waves) {
}
