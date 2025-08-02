package io.github.trimax.venta.engine.model.dto;

import lombok.NonNull;
import org.joml.Vector3f;

public record SceneObjectDTO(@NonNull String name,
                             Vector3f position,
                             Vector3f angles,
                             Vector3f scale) {
}
