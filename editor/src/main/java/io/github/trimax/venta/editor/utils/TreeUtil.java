package io.github.trimax.venta.editor.utils;

import io.github.trimax.venta.editor.definitions.Icons;
import io.github.trimax.venta.editor.model.Item;
import io.github.trimax.venta.editor.model.ItemType;
import io.github.trimax.venta.editor.tree.TreeCellRenderer;
import io.github.trimax.venta.editor.tree.TreeItemListener;
import javafx.collections.ListChangeListener;
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

        root.getChildren().add(new TreeItem<>(new Item(ItemType.Folder, Icons.MATERIAL, "Materials", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Folder, Icons.TEXTURE, "Textures", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Folder, Icons.PROGRAM, "Programs", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Folder, Icons.SHADER, "Shaders", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Folder, Icons.OBJECT, "Objects", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Folder, Icons.LIGHT, "Lights", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Folder, Icons.SCENE, "Scenes", null)));
        root.getChildren().add(new TreeItem<>(new Item(ItemType.Folder, Icons.MESH, "Meshes", null)));

        enableAutoSort(root);
    }

    public boolean isItemExist(@NonNull final TreeItem<Item> item, @NonNull final String name) {
        return StreamEx.of(item.getChildren())
                .map(TreeItem::getValue)
                .filterBy(Item::name, name)
                .findAny()
                .isPresent();
    }

    private void enableAutoSort(final TreeItem<Item> parent) {
        for (final var child : parent.getChildren())
            enableAutoSort(child);

        parent.getChildren().addListener((ListChangeListener<TreeItem<Item>>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (final var added : change.getAddedSubList())
                        enableAutoSort(added);

                    parent.getChildren().sort((a, b) -> a.getValue().name().compareToIgnoreCase(b.getValue().name()));
                }
            }
        });
    }
}
