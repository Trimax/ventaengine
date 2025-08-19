package io.github.trimax.venta.editor.handlers.resource;

import io.github.trimax.venta.editor.model.Item;
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
            status.setText("Select a file or a folder to remove");
            return;
        }

        final var item = selected.getValue();
        if (!item.type().isDeletable()) {
            status.setText("Neither root nor group can be removed");
            return;
        }

        selected.getParent().getChildren().remove(selected);
        status.setText("File `" + item.name() + "` removed");
    }
}
