package io.github.trimax.venta.engine.model.dto;

import io.github.trimax.venta.engine.enums.LightType;
import io.github.trimax.venta.engine.model.dto.common.AttenuationDTO;
import io.github.trimax.venta.engine.model.dto.common.ColorDTO;
import lombok.NonNull;

public record LightDTO(@NonNull ColorDTO color,
                       AttenuationDTO attenuation,
                       boolean castShadows,
                       float intensity,
                       float range,
                       LightType type,
                       Float innerAngle,
                       Float outerAngle) {
}