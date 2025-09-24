package io.github.trimax.venta.editor.handlers.archive;

import java.io.File;
import java.util.List;
import java.util.Map;

import io.github.trimax.venta.editor.events.archive.ArchiveLoadEvent;
import io.github.trimax.venta.editor.utils.DialogUtil;
import io.github.trimax.venta.editor.utils.EventUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

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
