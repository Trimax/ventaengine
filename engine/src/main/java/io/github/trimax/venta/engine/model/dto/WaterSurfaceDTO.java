package io.github.trimax.venta.engine.model.dto;

import java.util.List;

import io.github.trimax.venta.engine.model.dto.common.NoiseDTO;
import io.github.trimax.venta.engine.model.dto.common.WaveDTO;
import io.github.trimax.venta.engine.model.dto.water.WaterFoamDTO;
import io.github.trimax.venta.engine.model.dto.water.WaterMaterialDTO;
import lombok.NonNull;

public record WaterSurfaceDTO(@NonNull String gridMesh,
                              @NonNull String program,
                              @NonNull WaterMaterialDTO color,
                              @NonNull WaterFoamDTO foam,
                              List<NoiseDTO> noises,
                              List<WaveDTO> waves) {
}
