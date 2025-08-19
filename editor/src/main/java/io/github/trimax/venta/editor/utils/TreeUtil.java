package io.github.trimax.venta.editor.utils;

import io.github.trimax.venta.editor.definitions.Icons;
import io.github.trimax.venta.editor.listeners.TreeItemListener;
import io.github.trimax.venta.editor.model.Item;
import io.github.trimax.venta.editor.model.ItemType;
import io.github.trimax.venta.editor.renderers.TreeCellRenderer;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import one.util.streamex.StreamEx;

@UtilityClass
public final class TreeUtil {
    public void initialize(@NonNull final TreeView<Item> tree, @NonNull final TreeItemListener listener) {
        final var root = new TreeItem<>(new Item());
        tree.setCellFactory(_ -> new TreeCellRenderer());
        tree.setShowRoot(false);
        tree.setRoot(root);

        tree.getSelectionModel().selectedItemProperty()
                .addListener((_, _, newSel) -> listener.accept(newSel));

        root.getChildren().add(new TreeItem<>(new Item(ItemType.Group, Icons.MATERIAL, "Materials", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Group, Icons.TEXTURE, "Textures", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Group, Icons.SHADER, "Shaders", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Group, Icons.OBJECT, "Objects", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Group, Icons.MESH, "Meshes", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Group, Icons.LIGHT, "Lights", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Group, Icons.SCENE, "Scenes", null)));
    }

    public boolean isItemExist(@NonNull final TreeItem<Item> item, @NonNull final String name) {
        return StreamEx.of(item.getChildren())
                .map(TreeItem::getValue)
                .filterBy(Item::name, name)
                .findAny()
                .isPresent();
    }
}
