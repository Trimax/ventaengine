package io.github.trimax.venta.editor.listeners.main.folder;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.controllers.main.TreeController;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.model.event.folder.FolderRemoveEvent;
import io.github.trimax.venta.editor.model.event.status.StatusSetEvent;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FolderRemoveListener implements AbstractListener<FolderRemoveEvent> {
    private final TreeController treeController;

    @Override
    @Subscribe
    public void handle(@NonNull final FolderRemoveEvent event) {
        final var node = treeController.getSelectedNode();
        node.getParent().getChildren().remove(node);

        EventUtil.post(new StatusSetEvent("Folder `%s` removed", node.getValue().name()));
    }
}
