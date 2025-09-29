package io.github.trimax.venta.engine.model.dto.scene;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.dto.common.ColorDTO;
import lombok.NonNull;

public record SceneDirectionalLightDTO(@NonNull ColorDTO color,
                                       @NonNull Vector3f direction,
                                       float intensity) {
}