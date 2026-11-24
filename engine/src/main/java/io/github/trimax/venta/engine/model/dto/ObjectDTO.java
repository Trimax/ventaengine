package io.github.trimax.venta.engine.model.dto;

import java.util.List;

import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.model.dto.object.ObjectLodDTO;
import io.github.trimax.venta.engine.model.dto.object.ObjectMeshDTO;
import lombok.NonNull;

public record ObjectDTO(@NonNull String program,
                        @NonNull Node<ObjectMeshDTO> root,
                        String material,
                        List<ObjectLodDTO> lods) {
}