package io.github.trimax.venta.engine.model.dto;

import io.github.trimax.venta.core.model.common.Node;

public record ObjectPrefabDTO(String program, Node<MeshPrefabDTO> root) {
}