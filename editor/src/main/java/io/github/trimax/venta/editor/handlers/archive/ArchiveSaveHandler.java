package io.github.trimax.venta.editor.handlers.archive;

import io.github.trimax.venta.editor.model.Item;
import io.github.trimax.venta.editor.utils.DialogUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;

import java.io.File;

@AllArgsConstructor
public final class ArchiveSaveHandler implements EventHandler<ActionEvent> {
    private final TreeView<Item> tree;
    private final Label status;

    @Override
    public void handle(final ActionEvent event) {
        final var stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        DialogUtil.showFileSave("Please choose a file to save an archive", this::save, stage);
    }

    private void save(final File file) {
        status.setText("File saved to " + file.getAbsoluteFile());
    }
}
