package io.github.trimax.venta.engine.model.dto;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.enums.LightType;
import io.github.trimax.venta.engine.model.common.dto.Color;
import io.github.trimax.venta.engine.model.common.light.Attenuation;
import lombok.NonNull;

public record LightDTO(@NonNull LightType type,
                       @NonNull Color color,
                       Attenuation attenuation,
                       Vector3f direction,
                       boolean castShadows,
                       float intensity,
                       float range) {
}