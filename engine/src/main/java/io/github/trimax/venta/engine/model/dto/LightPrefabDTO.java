package io.github.trimax.venta.engine.model.dto;

import io.github.trimax.venta.engine.enums.LightType;
import io.github.trimax.venta.engine.model.common.dto.Color;
import io.github.trimax.venta.engine.model.common.light.Attenuation;
import org.joml.Vector3f;

public record LightPrefabDTO(LightType type,
                             Color color,
                             Vector3f direction,
                             float intensity,
                             Attenuation attenuation,
                             float range,
                             boolean castShadows) {
}