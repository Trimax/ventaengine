package io.github.trimax.venta.editor.model.dto;

import io.github.trimax.venta.core.model.common.Node;

public record BinaryArchiveDTO(String id, long timestamp, Node<byte[]> tree) {
}
