package io.github.trimax.venta.editor.model.dto;

import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.editor.definitions.Group;
import lombok.NonNull;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.Strings;

public record ArchiveDTO(String id, Node<String> tree) {
    public Node<String> getGroup(@NonNull final Group group) {
        if (tree == null || !tree.hasChildren())
            return null;

        return StreamEx.of(tree.children())
                .filter(c -> Strings.CS.equals(c.name(), group.name()))
                .findAny()
                .orElse(null);
    }
}
