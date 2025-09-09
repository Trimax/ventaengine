package io.github.trimax.venta.engine.model.dto;

import org.joml.Vector3f;

import lombok.NonNull;

public record EmitterDTO(int particlesCount,
                         float emissionRate,
                         float minimalSize,
                         float maximalSize,
                         float minimalLifetime,
                         float lifetimeDeviation,
                         Vector3f velocity,
                         Vector3f deviation,
                         @NonNull String texture) {
}
