package io.github.trimax.venta.editor.handlers.archive;

import java.io.File;
import java.util.List;
import java.util.Map;

import io.github.trimax.venta.editor.events.archive.ArchiveSaveEvent;
import io.github.trimax.venta.editor.utils.DialogUtil;
import io.github.trimax.venta.editor.utils.EventUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import lombok.SneakyThrows;

public final class ArchiveSaveHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(final ActionEvent event) {
        DialogUtil.showFileSave("Please choose a file to save an archive", this::save,
                Map.of("Archive files (*.json)", List.of("*.json")));
    }

    @SneakyThrows
    private void save(final File file) {
        EventUtil.post(new ArchiveSaveEvent(file));
    }
}
