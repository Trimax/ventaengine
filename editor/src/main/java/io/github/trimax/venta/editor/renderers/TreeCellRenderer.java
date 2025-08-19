package io.github.trimax.venta.editor.renderers;

import io.github.trimax.venta.editor.model.Item;
import javafx.scene.control.TreeCell;

public final class TreeCellRenderer extends TreeCell<Item> {
    @Override
    protected void updateItem(final Item item, final boolean empty) {
        super.updateItem(item, empty);
        if (item == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        setText(item.name());
        setGraphic(item.iconView());
    }
}
