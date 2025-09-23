package io.github.trimax.venta.engine.model.dto;

import org.joml.Vector3f;

import lombok.NonNull;

public record SceneSoundSourceDTO(@NonNull String name,
                                  @NonNull String sourceSource,
                                  @NonNull Vector3f position) {
}
