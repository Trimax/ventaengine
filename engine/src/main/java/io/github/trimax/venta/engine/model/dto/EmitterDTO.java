package io.github.trimax.venta.engine.model.dto;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.common.shared.Range;
import io.github.trimax.venta.engine.model.common.shared.Variable;
import lombok.NonNull;

public record EmitterDTO(@NonNull String texture,
                         @NonNull Range<Float> particleSize,
                         @NonNull Variable<Float> particleLifetime,
                         @NonNull Variable<Vector3f> particleVelocity,
                         @NonNull Variable<Float> particleAngularVelocity,
                         int particlesCount,
                         float emissionRate) {
}
