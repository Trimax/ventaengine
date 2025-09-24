package io.github.trimax.venta.editor.handlers.resource;

import java.io.File;
import java.util.List;
import java.util.Map;

import io.github.trimax.venta.editor.events.resource.ResourceAddEvent;
import io.github.trimax.venta.editor.utils.DialogUtil;
import io.github.trimax.venta.editor.utils.EventUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public final class ResourceAddHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(final ActionEvent event) {
        DialogUtil.showFileOpen("Please choose a resource to add", this::resourceAdd,
                Map.of("All Files (*.*)", List.of("*.*")));
    }

    private void resourceAdd(final File file) {
        EventUtil.post(new ResourceAddEvent(file));
    }
}
