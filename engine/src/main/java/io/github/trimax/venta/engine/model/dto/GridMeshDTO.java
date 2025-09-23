package io.github.trimax.venta.engine.model.dto;

import java.util.List;

import org.joml.Vector2i;

import io.github.trimax.venta.engine.model.dto.common.WaveDTO;
import lombok.NonNull;

public record GridMeshDTO(@NonNull Vector2i size,
                          @NonNull Vector2i segments,
                          @NonNull String program,
                          @NonNull String material,
                          List<WaveDTO> waves,
                          String heightmap) {
}
