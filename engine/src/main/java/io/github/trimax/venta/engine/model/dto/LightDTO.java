package io.github.trimax.venta.engine.model.dto;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.enums.LightType;

public record LightDTO(LightType type,
                       Vector3f position,
                       Vector3f direction,
                       Vector3f color,
                       float intensity,
                       AttenuationDTO attenuation,
                       float range,
                       boolean castShadows) {

    public record AttenuationDTO(float constant, float linear, float quadratic) {}
}