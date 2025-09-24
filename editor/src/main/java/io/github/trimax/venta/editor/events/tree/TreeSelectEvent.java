package io.github.trimax.venta.editor.events.tree;

import io.github.trimax.venta.editor.model.tree.Item;
import javafx.scene.control.TreeItem;

public record TreeSelectEvent(TreeItem<Item> node) {
    public boolean hasSelected() {
        return node != null;
    }

    public Item getItem() {
        return node.getValue();
    }
}
