package io.github.trimax.venta.editor.services;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.controllers.TreeController;
import io.github.trimax.venta.editor.events.group.GroupAddEvent;
import io.github.trimax.venta.editor.events.group.GroupRemoveEvent;
import io.github.trimax.venta.editor.events.status.StatusSetEvent;
import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.utils.NameUtil;
import io.github.trimax.venta.editor.utils.TreeUtil;
import javafx.scene.control.TreeItem;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

//TODO: Replace with listeners
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class GroupService {
    private final TreeController treeController;

    @Subscribe
    public void handleGroupAdd(final GroupAddEvent event) {
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

    @Subscribe
    public void handleGroupRemove(final GroupRemoveEvent event) {
        final var node = treeController.getSelectedNode();
        node.getParent().getChildren().remove(node);

        EventUtil.post(new StatusSetEvent("Group `%s` removed", node.getValue().name()));
    }
}
