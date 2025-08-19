package io.github.trimax.venta.editor.handlers.editor;

import io.github.trimax.venta.editor.utils.DialogUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public final class EditorAboutHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(final ActionEvent event) {
        DialogUtil.showInfo("Resource archive editor for Venta Engine");
    }
}
