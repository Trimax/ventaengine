package io.github.trimax.venta.engine.model.dto.scene;

import org.joml.Vector3f;

import lombok.NonNull;

public record SceneObjectDTO(@NonNull String name,
                             @NonNull String object,
                             @NonNull Vector3f position,
                             Vector3f angles,
                             Vector3f scale) {
}
