package io.github.trimax.venta.editor.model.tree;

import io.github.trimax.venta.editor.definitions.Element;
import io.github.trimax.venta.editor.definitions.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public record Item(ItemType type, Image icon, String name, String reference) {
    public Item() {
        this(ItemType.Root, Element.Unknown.getIcon(), "Root", null);
    }

    public static Item asFolder(final Group group) {
        return new Item(ItemType.Folder, group.getIcon(), group.name(), null);
    }

    public static Item asGroup(final String name) {
        return new Item(ItemType.Group, Element.Folder.getIcon(), name, null);
    }

    public static Item asElement(final String name, final String reference, final Image icon) {
        return new Item(ItemType.Resource, icon, name, reference);
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
