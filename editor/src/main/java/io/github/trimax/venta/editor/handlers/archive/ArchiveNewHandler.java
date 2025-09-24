package io.github.trimax.venta.editor.handlers.archive;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.events.tree.TreeResetEvent;
import io.github.trimax.venta.editor.utils.DialogUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArchiveNewHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(final ActionEvent event) {
        DialogUtil.showConfirm("Are you sure you want to create a new archive? All unsaved changes will be lost.", this::reset);
    }

    private void reset() {
        EventUtil.post(new TreeResetEvent());
    }
}
