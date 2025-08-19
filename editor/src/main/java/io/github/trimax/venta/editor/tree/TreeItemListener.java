package io.github.trimax.venta.editor.tree;

import io.github.trimax.venta.editor.model.Item;
import io.github.trimax.venta.editor.model.ToolBar;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;
import lombok.AllArgsConstructor;

import java.util.function.Consumer;

@AllArgsConstructor
public final class TreeItemListener implements Consumer<TreeItem<Item>> {
    private final ToolBar toolBar;
    private final VBox info;

    @Override
    public void accept(final TreeItem<Item> selected) {
        toolBar.update(selected.getValue());
        updateInfoPanel(selected);
    }

    private void updateInfoPanel(final TreeItem<Item> selected) {
        info.getChildren().clear();
        if (selected != null) {
            final var nameLabel = new Label("Name: " + selected.getValue().name());
            final var typeLabel = new Label(selected.getValue().type().name());
            info.getChildren().addAll(nameLabel, typeLabel);
        }
    }
}
