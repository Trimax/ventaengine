package io.github.trimax.venta.engine.model.common.geo;

import org.joml.Vector2f;

public record Wave(Vector2f direction,
                   float amplitude,
                   float steepness,
                   float frequency,
                   float length,
                   float speed,
                   float persistence,
                   float lacunarity,
                   int iterations) {

}