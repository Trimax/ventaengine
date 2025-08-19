package io.github.trimax.venta.editor.model;

import io.github.trimax.venta.editor.definitions.Icons;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public record Item(ItemType type, Image icon, String name, String reference) {
    public Item() {
        this(ItemType.Root, Icons.UNKNOWN, "Root", null);
    }

    public Item(final String name) {
        this(ItemType.Folder, Icons.FOLDER, name, null);
    }

    public Item(final Image icon, final String name, final String reference) {
        this(ItemType.File, icon, name, reference);
    }

    public ImageView iconView() {
        final var view = new ImageView(icon());
        view.setFitWidth(16);
        view.setFitHeight(16);

        if (type() == ItemType.Group) {
            view.setFitWidth(32);
            view.setFitHeight(32);
        }

        return view;
    }
}
