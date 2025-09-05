package io.github.trimax.venta.editor.tree;

import io.github.trimax.venta.editor.model.tree.Item;
import javafx.scene.control.TreeCell;
import javafx.scene.text.Font;

import static com.sun.javafx.font.FontFactory.DEFAULT_FULLNAME;

public final class TreeCellRenderer extends TreeCell<Item> {
    @Override
    protected void updateItem(final Item item, final boolean empty) {
        super.updateItem(item, empty);
        if (item == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        setFont(new Font(DEFAULT_FULLNAME, item.deletable() ? 12 : 14));
        setGraphicTextGap(item.deletable() ? 4 : 6);
        setGraphic(item.iconView());
        setText(item.name());
    }
}
