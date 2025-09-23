package io.github.trimax.venta.editor.events;

import io.github.trimax.venta.editor.model.tree.Item;
import javafx.scene.control.TreeItem;
import lombok.NonNull;

public record ItemSelectedEvent(@NonNull TreeItem<Item> selectedItem) {
}
