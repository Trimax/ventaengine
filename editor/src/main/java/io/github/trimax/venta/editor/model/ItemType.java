package io.github.trimax.venta.editor.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ItemType {
    Root(false),
    Group(false),
    Folder(true),
    File(true);

    private final boolean isDeletable;
}
