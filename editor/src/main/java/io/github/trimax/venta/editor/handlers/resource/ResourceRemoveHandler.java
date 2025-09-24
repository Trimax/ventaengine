package io.github.trimax.venta.editor.handlers.resource;

import io.github.trimax.venta.editor.events.resource.ResourceRemoveEvent;
import io.github.trimax.venta.editor.utils.DialogUtil;
import io.github.trimax.venta.editor.utils.EventUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public final class ResourceRemoveHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(final ActionEvent event) {
        DialogUtil.showConfirm("Are you sure you want to delete resource?", this::resourceRemove);
    }

    private void resourceRemove() {
        EventUtil.post(new ResourceRemoveEvent());
    }
}
