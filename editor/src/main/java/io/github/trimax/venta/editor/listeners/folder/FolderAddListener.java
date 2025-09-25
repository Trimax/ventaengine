package io.github.trimax.venta.editor.listeners.folder;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.controllers.TreeController;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.model.event.folder.FolderAddEvent;
import io.github.trimax.venta.editor.model.event.status.StatusSetEvent;
import io.github.trimax.venta.editor.utils.NameUtil;
import io.github.trimax.venta.editor.utils.TreeUtil;
import io.github.trimax.venta.engine.model.common.resource.Item;
import javafx.scene.control.TreeItem;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FolderAddListener implements AbstractListener<FolderAddEvent> {
    private final TreeController treeController;

    @Override
    @Subscribe
    public void handle(@NonNull final FolderAddEvent event) {
        final var node = treeController.getSelectedNode();

        if (StringUtils.isBlank(event.name()) || !NameUtil.isValidName(event.name())) {
            EventUtil.post(new StatusSetEvent("Folder name `%s` is incorrect. Name must contain only symbols, digits, -, _", event.name()));
            return;
        }

        if (TreeUtil.isItemExist(node, event.name())) {
            EventUtil.post(new StatusSetEvent("Folder `%s` already exists", event.name()));
            return;
        }

        final var group = new TreeItem<>(Item.asFolder(event.name()));
        node.getChildren().add(group);
        node.setExpanded(true);

        EventUtil.post(new StatusSetEvent("Folder `%s` created", event.name()));
    }
}
