package io.github.trimax.venta.editor.listeners.group;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.controllers.TreeController;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.model.event.group.GroupAddEvent;
import io.github.trimax.venta.editor.model.event.status.StatusSetEvent;
import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.utils.NameUtil;
import io.github.trimax.venta.editor.utils.TreeUtil;
import javafx.scene.control.TreeItem;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class GroupAddListener implements AbstractListener<GroupAddEvent> {
    private final TreeController treeController;

    @Override
    @Subscribe
    public void handle(@NonNull final GroupAddEvent event) {
        final var node = treeController.getSelectedNode();

        if (StringUtils.isBlank(event.name()) || !NameUtil.isValidName(event.name())) {
            EventUtil.post(new StatusSetEvent("Group name is incorrect. Name must contain only symbols, digits, -, _"));
            return;
        }

        if (TreeUtil.isItemExist(node, event.name())) {
            EventUtil.post(new StatusSetEvent("Group already exists with name " + event.name()));
            return;
        }

        final var group = new TreeItem<>(Item.asGroup(event.name()));
        node.getChildren().add(group);
        node.setExpanded(true);

        EventUtil.post(new StatusSetEvent("Group `%s` created", event.name()));
    }
}
