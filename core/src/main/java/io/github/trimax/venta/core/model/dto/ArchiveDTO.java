package io.github.trimax.venta.core.model.dto;

import io.github.trimax.venta.core.model.common.Node;

public record ArchiveDTO(IndexDTO index, Node<ResourceDTO> tree) {
}
