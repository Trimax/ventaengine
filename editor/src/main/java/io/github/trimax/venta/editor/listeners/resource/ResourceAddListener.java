package io.github.trimax.venta.editor.listeners.resource;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.controllers.TreeController;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.model.event.resource.ResourceAddEvent;
import io.github.trimax.venta.editor.model.event.status.StatusSetEvent;
import io.github.trimax.venta.engine.model.common.resource.Resource;
import javafx.scene.control.TreeItem;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceAddListener implements AbstractListener<ResourceAddEvent> {
    private final TreeController treeController;

    @Override
    @Subscribe
    public void handle(@NonNull final ResourceAddEvent event) {
        final var node = treeController.getSelectedNode();

        final var resource = new TreeItem<>(Resource.asResource(event.file().getName(), event.file().getAbsolutePath()));
        node.getChildren().add(resource);
        node.setExpanded(true);

        EventUtil.post(new StatusSetEvent("Resource `%s` added", event.file().getAbsolutePath()));
    }
}
