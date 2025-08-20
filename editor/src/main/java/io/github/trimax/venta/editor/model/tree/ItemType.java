package io.github.trimax.venta.editor.model.tree;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ItemType {
    Root(false, true),
    Folder(false, true),
    Group(true, true),
    Resource(true, false);

    private final boolean isDeletable;
    private final boolean isContainer;
}
