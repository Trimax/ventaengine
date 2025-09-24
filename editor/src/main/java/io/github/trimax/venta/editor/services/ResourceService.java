package io.github.trimax.venta.editor.services;

import java.io.File;

import io.github.trimax.venta.editor.events.status.StatusSetEvent;
import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.utils.EventUtil;
import javafx.scene.control.TreeItem;
import lombok.NonNull;

public final class ResourceService {
    public void add(@NonNull final File file, @NonNull final TreeItem<Item> node) {
        final var resource = new TreeItem<>(Item.asResource(file.getName(), file.getAbsolutePath()));
        node.getChildren().add(resource);
        node.setExpanded(true);

        EventUtil.post(new StatusSetEvent("Resource `%s` added", file.getAbsolutePath()));
    }

    public void remove(@NonNull final TreeItem<Item> node) {
        node.getParent().getChildren().remove(node);

        EventUtil.post(new StatusSetEvent("Resource `%s` removed", node.getValue().name()));
    }
}
