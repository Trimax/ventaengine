package io.github.trimax.venta.engine.model.dto;

import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.model.dto.object.ObjectMeshDTO;
import lombok.NonNull;

public record ObjectDTO(@NonNull String program,
                        @NonNull Node<ObjectMeshDTO> root,
                        String material) {
}