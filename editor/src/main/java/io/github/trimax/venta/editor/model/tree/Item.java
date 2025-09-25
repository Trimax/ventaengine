package io.github.trimax.venta.editor.model.tree;

import io.github.trimax.venta.editor.definitions.Element;
import io.github.trimax.venta.engine.enums.ResourceType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public record Item(ItemType type, Image icon, String name, String reference) {
    public static Item asGroup(final ResourceType type) {
        return new Item(ItemType.Group, getImage(type.getIcon()), type.getDisplayName(), null);
    }

    public static Item asFolder(final String name) {
        return new Item(ItemType.Folder, Element.Folder.getIcon(), name, null);
    }

    public static Item asResource(final String name, final String reference) {
        return new Item(ItemType.Resource, Element.Resource.getIcon(), name, reference);
    }

    public ImageView iconView() {
        final var view = new ImageView(icon());
        view.setFitWidth(16);
        view.setFitHeight(16);

        if (type == ItemType.Group) {
            view.setFitWidth(32);
            view.setFitHeight(32);
        }

        return view;
    }

    public boolean hasExistingReference() {
        return StringUtils.isNotBlank(reference) && Files.exists(Path.of(reference));
    }

    private static Image getImage(@NonNull final String path) {
        return new Image(java.util.Objects.requireNonNull(Item.class.getResourceAsStream(path)));
    }
}
