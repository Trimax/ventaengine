package io.github.trimax.venta.editor.utils;

import io.github.trimax.venta.editor.definitions.Folder;
import io.github.trimax.venta.editor.model.tree.Item;
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

        StreamEx.of(Folder.values())
                .map(Item::asFolder)
                .map(TreeItem::new)
                .forEach(root.getChildren()::add);

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
