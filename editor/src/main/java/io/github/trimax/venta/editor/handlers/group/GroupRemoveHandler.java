package io.github.trimax.venta.editor.handlers.group;

import io.github.trimax.venta.editor.events.group.GroupRemoveEvent;
import io.github.trimax.venta.editor.utils.DialogUtil;
import io.github.trimax.venta.editor.utils.EventUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public final class GroupRemoveHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(final ActionEvent event) {
        DialogUtil.showConfirm("Are you sure you want to delete group and all its contents?", this::groupRemove);
    }

    private void groupRemove() {
        EventUtil.post(new GroupRemoveEvent());
    }
}
