package com.venta.engine.model.dto;

import org.joml.Vector3f;
import org.joml.Vector4f;

import com.venta.engine.enums.LightType;

public record LightDTO(LightType type,
                       Vector3f position,
                       Vector3f direction,
                       Vector4f color,
                       float intensity,
                       AttenuationDTO attenuationDTO,
                       float range,
                       boolean castShadows) {

    public record AttenuationDTO(float constant, float linear, float quadratic) {}
}