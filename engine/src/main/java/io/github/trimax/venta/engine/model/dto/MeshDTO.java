package io.github.trimax.venta.engine.model.dto;

import io.github.trimax.venta.engine.model.common.math.Transform;

public record MeshDTO(String name,
                      String mesh,
                      String material,
                      Transform transform) {
}