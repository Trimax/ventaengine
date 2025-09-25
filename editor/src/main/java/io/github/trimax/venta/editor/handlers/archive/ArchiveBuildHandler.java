package io.github.trimax.venta.editor.handlers.archive;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.model.event.archive.ArchiveBuildEvent;
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
public final class ArchiveBuildHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(final ActionEvent event) {
        DialogUtil.showFileSave("Please choose a file to save built archive", this::build,
                Map.of("Binary archive files (*.vea)", List.of("*.vea")));
    }

    private void build(final File file) {
        EventUtil.post(new ArchiveBuildEvent(file));
    }
}
