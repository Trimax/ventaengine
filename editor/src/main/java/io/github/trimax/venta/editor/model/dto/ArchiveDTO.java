package io.github.trimax.venta.editor.model.dto;

import org.apache.commons.lang3.Strings;

import io.github.trimax.venta.core.model.common.Node;
import io.github.trimax.venta.editor.definitions.Folder;
import lombok.NonNull;
import one.util.streamex.StreamEx;

public record ArchiveDTO(MetaDTO meta, Node<String> tree) {
    public Node<String> getGroup(@NonNull final Folder folder) {
        if (tree == null || !tree.hasChildren())
            return null;

        return StreamEx.of(tree.children())
                .filter(c -> Strings.CS.equals(c.name(), folder.name()))
                .findAny()
                .orElse(null);
    }
}
