package io.github.trimax.venta.editor.model.tree;

import io.github.trimax.venta.editor.definitions.Element;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public record Item(ItemType type, Image icon, String name, String reference) {
    public Item() {
        this(ItemType.Root, Element.Unknown.getIcon(), "Root", null);
    }

    public Item(final String name) {
        this(ItemType.Group, Element.Folder.getIcon(), name, null);
    }

    public Item(final String name, final Image icon, final String reference) {
        this(ItemType.Resource, icon, name, reference);
    }

    public ImageView iconView() {
        final var view = new ImageView(icon());
        view.setFitWidth(16);
        view.setFitHeight(16);

        if (type() == ItemType.Folder) {
            view.setFitWidth(32);
            view.setFitHeight(32);
        }

        return view;
    }
}
