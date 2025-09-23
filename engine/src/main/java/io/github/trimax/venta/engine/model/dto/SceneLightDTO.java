package io.github.trimax.venta.engine.model.dto;

import org.joml.Vector3f;

import lombok.NonNull;

public record SceneLightDTO(@NonNull String name,
                            @NonNull String light,
                            @NonNull Vector3f position,
                            Vector3f direction) {
}
