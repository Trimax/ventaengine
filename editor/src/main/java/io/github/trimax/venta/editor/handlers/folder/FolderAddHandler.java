package io.github.trimax.venta.editor.handlers.folder;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.model.event.folder.FolderAddEvent;
import io.github.trimax.venta.editor.utils.DialogUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FolderAddHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(final ActionEvent event) {
        DialogUtil.showInput("Enter folder name:", "Create folder", "Name", this::addGroup);
    }

    private void addGroup(final String name) {
        EventUtil.post(new FolderAddEvent(name));
    }
}