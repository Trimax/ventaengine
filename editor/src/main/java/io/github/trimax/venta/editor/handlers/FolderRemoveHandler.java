package io.github.trimax.venta.editor.handlers;

import io.github.trimax.venta.editor.model.Item;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TreeView;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class FolderRemoveHandler implements EventHandler<ActionEvent> {
    private final TreeView<Item> tree;
    private final Label status;

    @Override
    public void handle(ActionEvent event) {
        final var selected = tree.getSelectionModel().getSelectedItem();

        if (selected != null && selected != tree.getRoot() && !selected.getChildren().isEmpty()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete folder and all its contents?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    selected.getParent().getChildren().remove(selected);
                    status.setText("Folder removed");
                }
            });
        } else if (selected != null && selected != tree.getRoot()) {
            // Empty folder
            selected.getParent().getChildren().remove(selected);
            status.setText("Folder removed");
        } else {
            status.setText("Select a folder to remove");
        }
    }
}
