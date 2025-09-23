package io.github.trimax.venta.engine.model.dto.common;

import org.joml.Vector2f;

import lombok.NonNull;

public record WaveDTO(@NonNull Vector2f direction,
                      float amplitude,
                      float steepness,
                      float length,
                      float speed) {
}
