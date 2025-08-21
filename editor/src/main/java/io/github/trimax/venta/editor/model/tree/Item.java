package io.github.trimax.venta.editor.model.tree;

import io.github.trimax.venta.editor.definitions.Element;
import io.github.trimax.venta.editor.definitions.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public record Item(ItemType type, Image icon, String name, String reference, boolean deletable) {
    public Item() {
        this(ItemType.Root, Element.Unknown.getIcon(), "Root", null, false);
    }

    public static Item asGroup(final Group group) {
        return new Item(ItemType.Group, group.getIcon(), group.name(), null, false);
    }

    public static Item asGroup(final String name) {
        return new Item(ItemType.Group, Element.Group.getIcon(), name, null, true);
    }

    public static Item asResource(final String name, final String reference) {
        return new Item(ItemType.Resource, Element.Resource.getIcon(), name, reference, true);
    }

    public ImageView iconView() {
        final var view = new ImageView(icon());
        view.setFitWidth(16);
        view.setFitHeight(16);

        if (!deletable()) {
            view.setFitWidth(32);
            view.setFitHeight(32);
        }

        return view;
    }

    public boolean hasExistingReference() {
        return StringUtils.isNotBlank(reference) && Files.exists(Path.of(reference));
    }
}
