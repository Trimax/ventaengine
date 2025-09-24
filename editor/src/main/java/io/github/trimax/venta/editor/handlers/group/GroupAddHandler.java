package io.github.trimax.venta.editor.handlers.group;

import io.github.trimax.venta.editor.events.group.GroupAddEvent;
import io.github.trimax.venta.editor.utils.DialogUtil;
import io.github.trimax.venta.editor.utils.EventUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public final class GroupAddHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(final ActionEvent event) {
        DialogUtil.showInput("Enter group name:", "Create group", "Name", this::addGroup);
    }

    private void addGroup(final String name) {
        EventUtil.post(new GroupAddEvent(name));
    }
}