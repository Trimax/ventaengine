package io.github.trimax.venta.editor.handlers.resource;

import io.github.trimax.venta.editor.model.tree.Item;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.TreeView;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class ResourceRemoveHandler implements EventHandler<ActionEvent> {
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

        selected.getParent().getChildren().remove(selected);
        status.setText("Resource `" + item.name() + "` removed");
    }
}
