package io.github.trimax.venta.editor.services;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.events.status.StatusSetEvent;
import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.utils.NameUtil;
import io.github.trimax.venta.editor.utils.TreeUtil;
import javafx.scene.control.TreeItem;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GroupService {
    public void add(@NonNull final String name, @NonNull final TreeItem<Item> node) {
        if (StringUtils.isBlank(name) || !NameUtil.isValidName(name)) {
            EventUtil.post(new StatusSetEvent("Group name is incorrect. Name must contain only symbols, digits, -, _"));
            return;
        }

        if (TreeUtil.isItemExist(node, name)) {
            EventUtil.post(new StatusSetEvent("Group already exists with name " + name));
            return;
        }

        final var group = new TreeItem<>(Item.asGroup(name));
        node.getChildren().add(group);
        node.setExpanded(true);

        EventUtil.post(new StatusSetEvent("Group `%s` created", name));
    }

    public void remove(@NonNull final TreeItem<Item> node) {
        node.getParent().getChildren().remove(node);

        EventUtil.post(new StatusSetEvent("Group `%s` removed", node.getValue().name()));
    }
}
