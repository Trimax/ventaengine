package com.venta.engine.model.dto;

import com.venta.engine.enums.LightType;
import org.joml.Vector3f;

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