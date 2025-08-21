package io.github.trimax.venta.editor.model.tree;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ItemType {
    Root(true),
    Group(true),
    Resource(false);

    private final boolean isContainer;
}
