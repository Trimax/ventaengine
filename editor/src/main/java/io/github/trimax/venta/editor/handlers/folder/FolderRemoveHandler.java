package io.github.trimax.venta.editor.handlers.folder;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.model.event.folder.FolderRemoveEvent;
import io.github.trimax.venta.editor.utils.DialogUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FolderRemoveHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(final ActionEvent event) {
        DialogUtil.showConfirm("Are you sure you want to delete folder and all its contents?", this::groupRemove);
    }

    private void groupRemove() {
        EventUtil.post(new FolderRemoveEvent());
    }
}
