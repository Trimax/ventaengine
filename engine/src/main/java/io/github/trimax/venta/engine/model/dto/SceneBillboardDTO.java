package io.github.trimax.venta.engine.model.dto;

import org.joml.Vector2f;
import org.joml.Vector3f;

import lombok.NonNull;

public record SceneBillboardDTO(@NonNull String name,
                                @NonNull String billboard,
                                @NonNull Vector3f position,
                                @NonNull Vector2f size) {
}
