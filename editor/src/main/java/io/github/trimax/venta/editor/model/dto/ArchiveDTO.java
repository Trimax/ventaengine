package io.github.trimax.venta.editor.model.dto;

import org.apache.commons.lang3.Strings;

import io.github.trimax.venta.core.model.common.Node;
import lombok.NonNull;
import one.util.streamex.StreamEx;

public record ArchiveDTO(MetaDTO meta, Node<ResourceDTO> tree) {
    public Node<ResourceDTO> getGroup(@NonNull final String name) {
        if (tree == null || !tree.hasChildren())
            return null;

        return StreamEx.of(tree.children())
                .filter(c -> Strings.CS.equals(c.name(), name))
                .findAny()
                .orElse(null);
    }
}
