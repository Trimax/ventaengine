package io.github.trimax.venta.editor.handlers.group;

import io.github.trimax.venta.editor.model.Item;
import io.github.trimax.venta.editor.model.ItemType;
import io.github.trimax.venta.editor.utils.DialogUtil;
import io.github.trimax.venta.editor.utils.NameUtil;
import io.github.trimax.venta.editor.utils.TreeUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
public final class GroupAddHandler implements EventHandler<ActionEvent> {
    private final TreeView<Item> tree;
    private final Label status;

    @Override
    public void handle(final ActionEvent event) {
        final var selected = tree.getSelectionModel().getSelectedItem();
        if (selected == null) {
            status.setText("Please select an item where to create a group");
            return;
        }

        final var item = selected.getValue();
        if (item.type() == ItemType.Root || item.type() == ItemType.Resource) {
            status.setText("Group can't be created under that item");
            return;
        }

        DialogUtil.showInput("Enter group name:", "Create group", "Name", name -> addFolder(name, selected));
    }

    private void addFolder(final String name, final TreeItem<Item> selected) {
        if (StringUtils.isBlank(name) || !NameUtil.isValidName(name)) {
            status.setText("Group name is incorrect. Name must contain only symbols, digits, -, _");
            return;
        }

        if (TreeUtil.isItemExist(selected, name)) {
            status.setText("Group with this name already exist");
            return;
        }

        final var newFolder = new TreeItem<>(new Item(name));
        selected.getChildren().add(newFolder);
        selected.setExpanded(true);
        status.setText("Group `" + name + "` created");
    }
}
