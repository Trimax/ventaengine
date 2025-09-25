package io.github.trimax.venta.editor.listeners.group;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.controllers.TreeController;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.model.event.group.GroupRemoveEvent;
import io.github.trimax.venta.editor.model.event.status.StatusSetEvent;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class GroupRemoveListener implements AbstractListener<GroupRemoveEvent> {
    private final TreeController treeController;

    @Override
    @Subscribe
    public void handle(@NonNull final GroupRemoveEvent event) {
        final var node = treeController.getSelectedNode();
        node.getParent().getChildren().remove(node);

        EventUtil.post(new StatusSetEvent("Group `%s` removed", node.getValue().name()));
    }
}
