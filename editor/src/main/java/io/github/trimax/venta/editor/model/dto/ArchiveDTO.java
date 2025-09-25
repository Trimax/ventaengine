package io.github.trimax.venta.editor.model.dto;

import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.enums.ResourceType;
import lombok.NonNull;

import java.util.Map;

public record ArchiveDTO(String id, Map<ResourceType, Node<String>> groups) {
    public Node<String> getGroup(@NonNull final ResourceType type) {
        return groups.getOrDefault(type, null);
    }
}
