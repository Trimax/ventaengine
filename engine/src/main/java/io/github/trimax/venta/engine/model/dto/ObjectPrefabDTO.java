package io.github.trimax.venta.engine.model.dto;

import io.github.trimax.venta.engine.model.common.hierarchy.Node;

public record ObjectPrefabDTO(String program, Node<MeshPrefabDTO> root) {
}