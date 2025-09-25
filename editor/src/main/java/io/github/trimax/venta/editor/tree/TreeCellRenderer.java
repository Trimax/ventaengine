package io.github.trimax.venta.editor.tree;

import io.github.trimax.venta.editor.utils.IconUtil;
import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.engine.model.common.resource.Item;
import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public final class TreeCellRenderer extends TreeCell<Item> {
    @Override
    protected void updateItem(final Item item, final boolean empty) {
        super.updateItem(item, empty);
        if (item == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        switch (item.type()) {
            case Group -> setGraphic(getIcon(IconUtil.getResourceImage(GroupType.valueOf(item.reference()))));
            case Folder, File -> setGraphic(getIcon(IconUtil.getTypeImage(item.type())));
            default -> log.warn("The icon hasn't been set for item type {}", item.type());
        }

        setText(item.name());
    }

    private ImageView getIcon(@NonNull final Image image) {
        final var view = new ImageView(image);
        view.setFitWidth(16);
        view.setFitHeight(16);

        return view;
    }
}
