package io.github.trimax.venta.engine.model.dto.scene;

import org.joml.Vector3f;

import lombok.NonNull;

public record SceneEmitterDTO(@NonNull String name,
                              @NonNull String emitter,
                              @NonNull Vector3f position) {
}
