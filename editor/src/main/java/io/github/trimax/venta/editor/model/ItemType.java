package io.github.trimax.venta.editor.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ItemType {
    Root(false, false),
    Group(false, true),
    Folder(true, true),
    File(true, false);

    private final boolean isDeletable;
    private final boolean isContainer;
}
