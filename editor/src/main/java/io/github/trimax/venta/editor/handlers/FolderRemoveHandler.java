package io.github.trimax.venta.editor.handlers;

import io.github.trimax.venta.editor.model.Item;
import io.github.trimax.venta.editor.utils.DialogUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class FolderRemoveHandler implements EventHandler<ActionEvent> {
    private final TreeView<Item> tree;
    private final Label status;

    @Override
    public void handle(final ActionEvent event) {
        final var selected = tree.getSelectionModel().getSelectedItem();
        if (selected == null) {
            status.setText("Select a file or a folder to delete it");
            return;
        }

        final var item = selected.getValue();
        if (!item.type().isDeletable()) {
            status.setText("This item can not be deleted");
            return;
        }

        DialogUtil.showConfirm("Are you sure you want to delete folder `" + item.name() + "` and all its contents?",
                () -> removeFolder(selected));
    }

    private void removeFolder(final TreeItem<Item> selected) {
        selected.getParent().getChildren().remove(selected);
        status.setText("File `" + selected.getValue().name() + "` removed");
    }
}
