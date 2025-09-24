package io.github.trimax.venta.editor.services;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.controllers.TreeController;
import io.github.trimax.venta.editor.events.resource.ResourceAddEvent;
import io.github.trimax.venta.editor.events.resource.ResourceRemoveEvent;
import io.github.trimax.venta.editor.events.status.StatusSetEvent;
import io.github.trimax.venta.editor.model.tree.Item;
import javafx.scene.control.TreeItem;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

//TODO: Replace with listeners
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceService {
    private final TreeController treeController;

    @Subscribe
    public void onResourceAdd(final ResourceAddEvent event) {
        final var node = treeController.getSelectedNode();

        final var resource = new TreeItem<>(Item.asResource(event.file().getName(), event.file().getAbsolutePath()));
        node.getChildren().add(resource);
        node.setExpanded(true);

        EventUtil.post(new StatusSetEvent("Resource `%s` added", event.file().getAbsolutePath()));
    }

    @Subscribe
    public void onResourceRemove(final ResourceRemoveEvent event) {
        final var node = treeController.getSelectedNode();
        node.getParent().getChildren().remove(node);

        EventUtil.post(new StatusSetEvent("Resource `%s` removed", node.getValue().name()));
    }
}
