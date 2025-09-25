package io.github.trimax.venta.editor.listeners.archive;

import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.controllers.TreeController;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.model.dto.ArchiveDTO;
import io.github.trimax.venta.editor.model.event.archive.ArchiveSaveEvent;
import io.github.trimax.venta.editor.model.event.status.StatusSetEvent;
import io.github.trimax.venta.editor.utils.TreeUtil;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.FileWriter;
import java.util.UUID;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArchiveSaveListener implements AbstractListener<ArchiveSaveEvent> {
    private final TreeController treeController;

    @Override
    @Subscribe
    @SneakyThrows
    public void handle(@NonNull final ArchiveSaveEvent event) {
        try (final var writer = new FileWriter(event.file())) {
            new Gson().toJson(new ArchiveDTO(UUID.randomUUID().toString(), TreeUtil.createNode(treeController.getRoot())), writer);
        }

        EventUtil.post(new StatusSetEvent("Archive saved to %s", event.file().getAbsoluteFile()));
    }
}
