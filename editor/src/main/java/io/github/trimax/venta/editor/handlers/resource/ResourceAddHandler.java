package io.github.trimax.venta.editor.handlers.resource;

import io.github.trimax.venta.editor.definitions.Icons;
import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.model.tree.ItemType;
import io.github.trimax.venta.editor.utils.DialogUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;

import java.io.File;

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

        final var stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        DialogUtil.showFileOpen("Please choose a resource to add", file -> addResource(file, selected), stage);
    }

    private void addResource(final File file, final TreeItem<Item> selected) {
        final var newFile = new TreeItem<>(new Item(Icons.FILE, file.getName(), file.getAbsolutePath()));
        selected.getChildren().add(newFile);
        selected.setExpanded(true);
        status.setText("Resource `" + file.getName() + "` added");
    }
}
