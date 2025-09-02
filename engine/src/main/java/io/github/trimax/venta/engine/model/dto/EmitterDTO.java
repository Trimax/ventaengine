package io.github.trimax.venta.engine.model.dto;

import lombok.NonNull;
import org.joml.Vector3f;

public record EmitterDTO(int particlesCount,
                         float emissionRate,
                         Vector3f velocity,
                         Vector3f deviation,
                         @NonNull String texture) {
}
