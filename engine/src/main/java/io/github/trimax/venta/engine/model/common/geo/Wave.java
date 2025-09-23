package io.github.trimax.venta.engine.model.common.geo;

import org.joml.Vector2f;

public record Wave(Vector2f direction,
                   float amplitude,
                   float steepness,
                   float length,
                   float speed) {
}