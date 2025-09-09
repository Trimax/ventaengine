package io.github.trimax.venta.engine.model.dto;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.common.dto.Range;
import io.github.trimax.venta.engine.model.common.dto.Variable;
import lombok.NonNull;

public record EmitterDTO(int particlesCount,
                         float emissionRate,
                         Range<Float> particleSize,
                         Variable<Vector3f> particleVelocity,
                         Variable<Float> particleLifetime,
                         Variable<Float> particleAngularVelocity,
                         @NonNull String texture) {
}
