package io.github.trimax.venta.engine.model.dto;

import java.util.List;

import org.joml.Vector2i;

import io.github.trimax.venta.engine.model.common.geo.Wave;
import lombok.NonNull;

public record GridMeshDTO(@NonNull Vector2i size,
                          @NonNull Vector2i segments,
                          List<Wave> waves,
                          String program,
                          String heightmap) {
}
