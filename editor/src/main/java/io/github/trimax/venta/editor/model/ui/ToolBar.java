package io.github.trimax.venta.editor.model.ui;

import io.github.trimax.venta.editor.model.tree.Item;
import javafx.scene.control.Button;
import lombok.Builder;
import lombok.NonNull;

@Builder
public final class ToolBar {
    @NonNull
    private final Button btnTreeResourceAdd;

    @NonNull
    private Button btnTreeResourceRemove;

    @NonNull
    private Button btnTreeFolderAdd;

    @NonNull
    private Button btnTreeFolderRemove;

    public void update(final Item selected) {
        btnTreeResourceAdd.setDisable(selected == null || !selected.type().isContainer());
        btnTreeResourceRemove.setDisable(selected == null || selected.type().isContainer());

        btnTreeFolderAdd.setDisable(selected == null || !selected.type().isContainer());
        btnTreeFolderRemove.setDisable(selected == null || !selected.type().isContainer() || !selected.deletable());
    }
}
