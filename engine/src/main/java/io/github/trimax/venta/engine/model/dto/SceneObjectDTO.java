package io.github.trimax.venta.engine.model.dto;

import lombok.NonNull;
import org.joml.Vector3f;

public record SceneObjectDTO(@NonNull String name,
                             @NonNull String object,
                             Vector3f position,
                             Vector3f angles,
                             Vector3f scale) {
}
