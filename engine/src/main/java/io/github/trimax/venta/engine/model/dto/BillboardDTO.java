package io.github.trimax.venta.engine.model.dto;

import org.joml.Vector2f;

import lombok.NonNull;

public record BillboardDTO(@NonNull String program,
                           @NonNull String sprite,
                           Vector2f scale) {
}
