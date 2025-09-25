package io.github.trimax.venta.editor.model.dto;

import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.engine.model.common.resource.Item;
import lombok.NonNull;

import java.util.Map;

public record ArchiveDTO(String id, Map<GroupType, Node<Item>> groups) {
    public Node<Item> getGroup(@NonNull final GroupType type) {
        return groups.getOrDefault(type, null);
    }
}
