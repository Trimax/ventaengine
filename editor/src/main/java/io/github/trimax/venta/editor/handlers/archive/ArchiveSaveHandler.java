package io.github.trimax.venta.editor.handlers.archive;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.model.event.archive.ArchiveSaveEvent;
import io.github.trimax.venta.editor.utils.DialogUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.util.List;
import java.util.Map;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
