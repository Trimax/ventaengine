package io.github.trimax.venta.engine.model.dto;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.enums.LightType;
import io.github.trimax.venta.engine.model.common.light.Attenuation;

public record LightPrefabDTO(LightType type,
                             Vector3f color,
                             Vector3f direction,
                             float intensity,
                             Attenuation attenuation,
                             float range,
                             boolean castShadows) {
}