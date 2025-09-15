package io.github.trimax.venta.engine.model.dto;

import io.github.trimax.venta.engine.model.common.geo.Surface;
import io.github.trimax.venta.engine.model.common.geo.Wave;
import io.github.trimax.venta.engine.model.common.math.Fresnel;
import lombok.NonNull;
import org.joml.Vector2i;

public record GridMeshDTO(@NonNull Vector2i size,
                          @NonNull Vector2i segments,
                          @NonNull Wave wave,
                          @NonNull Surface surface,
                          @NonNull Surface trough,
                          @NonNull Surface peak,
                          @NonNull Fresnel fresnel,
                          String program,
                          String heightmap) {
}
