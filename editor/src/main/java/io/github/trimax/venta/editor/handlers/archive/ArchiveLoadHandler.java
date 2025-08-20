package io.github.trimax.venta.editor.handlers.archive;

import io.github.trimax.venta.editor.model.Item;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.TreeView;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class ArchiveLoadHandler implements EventHandler<ActionEvent> {
    private final TreeView<Item> tree;
    private final Label status;

    @Override
    public void handle(final ActionEvent event) {
        //DialogUtil.showConfirm("Are you sure you want to create a new archive? All unsaved changes will be lost.", this::reset);
    }

    private void reset() {
        //TreeUtil.initialize(tree, listener);
    }
}
