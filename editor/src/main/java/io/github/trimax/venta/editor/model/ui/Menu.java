package io.github.trimax.venta.editor.model.ui;

import io.github.trimax.venta.editor.model.tree.Item;
import javafx.scene.control.MenuItem;
import lombok.Builder;
import lombok.NonNull;

@Builder
public final class Menu {
    @NonNull
    private final MenuItem btnMenuResourceAdd;

    @NonNull
    private MenuItem btnMenuResourceRemove;

    @NonNull
    private MenuItem btnMenuGroupAdd;

    @NonNull
    private MenuItem btnMenuGroupRemove;

    public void update(final Item selected) {
        btnMenuResourceAdd.setDisable(selected == null || !selected.type().isContainer());
        btnMenuResourceRemove.setDisable(selected == null || selected.type().isContainer());

        btnMenuGroupAdd.setDisable(selected == null || !selected.type().isContainer());
        btnMenuGroupRemove.setDisable(selected == null || !selected.type().isContainer() || !selected.deletable());
    }
}
