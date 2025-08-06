package io.github.trimax.venta.engine.model.dto;

import io.github.trimax.venta.engine.model.math.Transform;

public record MeshPrefabDTO(String name, String mesh, String material, Transform transform) {
}