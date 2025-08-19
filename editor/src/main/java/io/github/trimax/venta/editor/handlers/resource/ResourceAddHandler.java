package io.github.trimax.venta.editor.handlers.resource;

import io.github.trimax.venta.editor.definitions.Icons;
import io.github.trimax.venta.editor.model.Item;
import io.github.trimax.venta.editor.model.ItemType;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class ResourceAddHandler implements EventHandler<ActionEvent> {
    private final TreeView<Item> tree;
    private final Label status;

    @Override
    public void handle(final ActionEvent event) {
        final var selected = tree.getSelectionModel().getSelectedItem();
        if (selected == null) {
            status.setText("Select a folder to add file");
            return;
        }

        if (selected.getValue().type() != ItemType.Group) {
            status.setText("Selected item is not a folder");
            return;
        }

        final var newFile = new TreeItem<>(new Item(Icons.FILE, "New File", "/path/to/file"));
        selected.getChildren().add(newFile);
        selected.setExpanded(true);
        status.setText("New file added");
    }
}
