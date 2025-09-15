package io.github.trimax.venta.engine.model.common.geo;

import io.github.trimax.venta.engine.model.common.dto.Color;

public record Surface(Color color,
                      float threshold,
                      float transition) {
}
