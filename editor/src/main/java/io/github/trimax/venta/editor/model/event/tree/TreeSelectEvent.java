package io.github.trimax.venta.editor.model.event.tree;

import io.github.trimax.venta.editor.model.event.AbstractEvent;
import io.github.trimax.venta.engine.model.common.resource.Item;
import javafx.scene.control.TreeItem;

public record TreeSelectEvent(TreeItem<Item> node) implements AbstractEvent {
    public boolean hasSelected() {
        return node != null;
    }

    public Item getItem() {
        return node.getValue();
    }
}
