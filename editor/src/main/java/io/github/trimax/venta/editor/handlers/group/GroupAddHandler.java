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
            status.setText("Select a group or a folder to create folder");
            return;
        }

        final var item = selected.getValue();
        if (item.type() == ItemType.Root || item.type() == ItemType.File) {
            status.setText("Neither root nor file can be used for creating folders");
            return;
        }

        DialogUtil.showInput("Enter folder name:", "Create folder", "Name", name -> addFolder(name, selected));
    }

    private void addFolder(final String name, final TreeItem<Item> selected) {
        if (StringUtils.isBlank(name) || !NameUtil.isValidName(name)) {
            status.setText("Folder name is incorrect. Must contain only symbols, digits, -, _");
            return;
        }

        if (TreeUtil.isItemExist(selected, name)) {
            status.setText("Folder with this name already exist");
            return;
        }

        final var newFolder = new TreeItem<>(new Item(name));
        selected.getChildren().add(newFolder);
        selected.setExpanded(true);
        status.setText("Folder `" + name + "` created");
    }
}
