package io.github.trimax.venta.editor.handlers.group;

import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.utils.DialogUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class GroupRemoveHandler implements EventHandler<ActionEvent> {
    private final TreeView<Item> tree;
    private final Label status;

    @Override
    public void handle(final ActionEvent event) {
        final var selected = tree.getSelectionModel().getSelectedItem();
        if (selected == null) {
            status.setText("Please select an item to remove");
            return;
        }

        final var item = selected.getValue();
        if (!item.deletable()) {
            status.setText("This item can not be deleted");
            return;
        }

        DialogUtil.showConfirm("Are you sure you want to delete group `" + item.name() + "` and all its contents?",
                () -> removeFolder(selected));
    }

    private void removeFolder(final TreeItem<Item> selected) {
        selected.getParent().getChildren().remove(selected);
        status.setText("Group `" + selected.getValue().name() + "` removed");
    }
}
