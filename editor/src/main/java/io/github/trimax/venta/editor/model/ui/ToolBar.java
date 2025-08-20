package io.github.trimax.venta.editor.model.ui;

import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.model.tree.ItemType;
import javafx.scene.control.Button;
import lombok.Builder;
import lombok.NonNull;

@Builder
public final class ToolBar {
    @NonNull
    private final Button btnToolBarResourceAdd;

    @NonNull
    private Button btnToolBarResourceRemove;

    @NonNull
    private Button btnToolBarGroupAdd;

    @NonNull
    private Button btnToolBarGroupRemove;

    public void update(final Item selected) {
        btnToolBarResourceAdd.setDisable(selected == null || selected.type() != ItemType.Group);
        btnToolBarResourceRemove.setDisable(selected == null || selected.type() != ItemType.Resource);
        btnToolBarGroupAdd.setDisable(selected == null || !selected.type().isContainer());
        btnToolBarGroupRemove.setDisable(selected == null || selected.type() != ItemType.Group);
    }
}
