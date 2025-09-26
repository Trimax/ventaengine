package io.github.trimax.venta.engine.model.dto.water;

import io.github.trimax.venta.engine.model.dto.common.ColorDTO;
import lombok.NonNull;

public record WaterMaterialDTO(@NonNull ColorDTO colorSurface,
                               @NonNull ColorDTO colorDepth,
                               @NonNull ColorDTO colorPeak,
                               float metalness,
                               float opacity) {
}
