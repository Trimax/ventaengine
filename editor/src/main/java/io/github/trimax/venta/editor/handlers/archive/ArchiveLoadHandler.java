package io.github.trimax.venta.editor.handlers.archive;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.model.event.archive.ArchiveLoadEvent;
import io.github.trimax.venta.editor.utils.DialogUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.List;
import java.util.Map;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArchiveLoadHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(final ActionEvent event) {
        DialogUtil.showFileOpen("Please choose an archive to load", this::load,
                Map.of("Archive files (*.json)", List.of("*.json")));
    }

    private void load(final File file) {
        EventUtil.post(new ArchiveLoadEvent(file));
    }
}
