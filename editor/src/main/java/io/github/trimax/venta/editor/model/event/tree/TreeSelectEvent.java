package io.github.trimax.venta.editor.model.event.tree;

import io.github.trimax.venta.editor.model.event.AbstractEvent;
import io.github.trimax.venta.engine.model.common.resource.Resource;
import javafx.scene.control.TreeItem;

public record TreeSelectEvent(TreeItem<Resource> node) implements AbstractEvent {
    public boolean hasSelected() {
        return node != null;
    }

    public Resource getItem() {
        return node.getValue();
    }
}
