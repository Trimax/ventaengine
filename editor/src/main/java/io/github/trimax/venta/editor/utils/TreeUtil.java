package io.github.trimax.venta.editor.utils;

import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.editor.definitions.Group;
import io.github.trimax.venta.editor.events.tree.TreeSelectEvent;
import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.tree.TreeCellRenderer;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import one.util.streamex.StreamEx;

@UtilityClass
public final class TreeUtil {
    public void initialize(@NonNull final TreeView<Item> tree) {
        final var root = new TreeItem<>(new Item());
        tree.setCellFactory(_ -> new TreeCellRenderer());
        tree.setShowRoot(false);
        tree.setRoot(root);

        tree.getSelectionModel().selectedItemProperty()
                .addListener((_, _, newSel) -> EventUtil.post(new TreeSelectEvent(newSel)));

        StreamEx.of(Group.values())
                .sorted()
                .map(Item::asGroup)
                .map(TreeItem::new)
                .forEach(root.getChildren()::add);

        enableAutoSort(root);
        sort(root);
    }

    public boolean isItemExist(@NonNull final TreeItem<Item> item, @NonNull final String name) {
        return StreamEx.of(item.getChildren())
                .map(TreeItem::getValue)
                .filterBy(Item::name, name)
                .findAny()
                .isPresent();
    }

    public Node<String> createNode(final TreeItem<Item> node) {
        return new Node<>(node.getValue().name(), node.getValue().reference(),
                node.getChildren().stream().map(TreeUtil::createNode).toList());
    }

    private void enableAutoSort(final TreeItem<Item> parent) {
        for (final var child : parent.getChildren())
            enableAutoSort(child);

        parent.getChildren().addListener((ListChangeListener<TreeItem<Item>>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (final var added : change.getAddedSubList())
                        enableAutoSort(added);
                    sort(parent);
                }
            }
        });
    }

    private void sort(final TreeItem<Item> node) {
        node.getChildren().sort((a, b) -> a.getValue().name().compareToIgnoreCase(b.getValue().name()));
    }
}
