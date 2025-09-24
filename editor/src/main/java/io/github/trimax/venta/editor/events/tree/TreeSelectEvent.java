package io.github.trimax.venta.editor.events.tree;

import io.github.trimax.venta.editor.model.tree.Item;
import javafx.scene.control.TreeItem;
import lombok.NonNull;

public record TreeSelectEvent(@NonNull TreeItem<Item> node) {
}
