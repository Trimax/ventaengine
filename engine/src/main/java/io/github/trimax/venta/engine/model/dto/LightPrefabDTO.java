package io.github.trimax.venta.engine.model.dto;

import io.github.trimax.venta.engine.enums.LightType;
import io.github.trimax.venta.engine.model.common.light.Attenuation;
import org.joml.Vector3f;

public record LightPrefabDTO(LightType type,
                             Vector3f color,
                             float intensity,
                             Attenuation attenuation,
                             float range,
                             boolean castShadows) {
}