package io.github.trimax.venta.editor.handlers.resource;

import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.utils.DialogUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.AllArgsConstructor;

import java.io.File;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public final class ResourceAddHandler implements EventHandler<ActionEvent> {
    private final TreeView<Item> tree;
    private final Label status;

    @Override
    public void handle(final ActionEvent event) {
        final var selected = tree.getSelectionModel().getSelectedItem();
        if (selected == null) {
            status.setText("Select a group to add the resource");
            return;
        }

        if (!selected.getValue().type().isContainer()) {
            status.setText("Selected item is not a group");
            return;
        }

        DialogUtil.showFileOpen("Please choose a resource to add", file -> addResource(file, selected),
                tree.getScene().getWindow(),
                Map.of("All Files (*.*)", List.of("*.*")));
    }

    private void addResource(final File file, final TreeItem<Item> selected) {
        final var resource = new TreeItem<>(Item.asResource(file.getName(), file.getAbsolutePath()));
        selected.getChildren().add(resource);
        selected.setExpanded(true);
        status.setText("Resource `" + file.getName() + "` added");
    }
}
