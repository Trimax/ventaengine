package io.github.trimax.venta.engine.model.dto;

import io.github.trimax.venta.container.tree.Node;

public record ObjectPrefabDTO(String program,
                              String material,
                              Node<MeshPrefabDTO> root) {
}