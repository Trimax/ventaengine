package io.github.trimax.venta.editor.handlers.editor;

import io.github.trimax.venta.editor.utils.DialogUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Window;
import one.util.streamex.StreamEx;

public final class EditorQuitHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(final ActionEvent event) {
        DialogUtil.showConfirm("Are you sure you want to close an editor? All unsaved changes will be lost.", this::exit);
    }

    private void exit() {
        StreamEx.of(Window.getWindows()).forEach(Window::hide);
        Platform.runLater(Platform::exit);
    }
}
