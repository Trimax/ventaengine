package io.github.trimax.venta.editor.handlers;

import io.github.trimax.venta.editor.model.Item;
import io.github.trimax.venta.editor.utils.DialogUtil;
import io.github.trimax.venta.editor.utils.TreeUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class ArchiveNewHandler implements EventHandler<ActionEvent> {
    private final TreeView<Item> tree;
    private final VBox info;

    @Override
    public void handle(final ActionEvent event) {
        DialogUtil.showConfirm("Are you sure you want to create a new archive? All unsaved changes will be lost.", this::reset);
    }

    private void reset() {
        TreeUtil.initialize(tree, info);
    }
}
