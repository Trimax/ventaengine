package io.github.trimax.venta.editor.model;

public record Item(ItemType type, String name, String reference) {
    public Item() {
        this(ItemType.Root, "Root", null);
    }

    public Item(final String name) {
        this(ItemType.Folder, name, null);
    }

    public Item(final String name, final String reference) {
        this(ItemType.File, name, reference);
    }
}
