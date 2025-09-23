package io.github.trimax.venta.engine.model.dto;

import lombok.NonNull;

public record ProgramDTO(@NonNull String shaderVertex,
                         @NonNull String shaderFragment) {
}