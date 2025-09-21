package io.github.trimax.venta.engine.model.dto;

import io.github.trimax.venta.engine.model.common.geo.Wave;
import lombok.NonNull;
import org.joml.Vector2i;

import java.util.List;

public record GridMeshDTO(@NonNull Vector2i size,
                          @NonNull Vector2i segments,
                          List<Wave> waves,
                          String program,
                          String material,
                          String heightmap) {
}
