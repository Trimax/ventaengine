package io.github.trimax.venta.engine.model.dto;

import io.github.trimax.venta.container.tree.Node;
import lombok.NonNull;

public record ObjectDTO(@NonNull String program,
                        @NonNull Node<MeshDTO> root,
                        String material) {
}