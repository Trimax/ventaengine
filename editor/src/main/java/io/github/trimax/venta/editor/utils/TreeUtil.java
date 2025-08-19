package io.github.trimax.venta.editor.utils;

import io.github.trimax.venta.editor.model.Item;
import javafx.scene.control.TreeItem;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import one.util.streamex.StreamEx;

@UtilityClass
public final class TreeUtil {
    public boolean isItemExist(@NonNull final TreeItem<Item> item, @NonNull final String name) {
        return StreamEx.of(item.getChildren())
                .map(TreeItem::getValue)
                .filterBy(Item::name, name)
                .findAny()
                .isPresent();
    }
}
