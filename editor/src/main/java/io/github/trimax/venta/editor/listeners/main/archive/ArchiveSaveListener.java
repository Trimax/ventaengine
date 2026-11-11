package io.github.trimax.venta.editor.listeners.main.archive;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.context.Context;

import io.github.trimax.venta.editor.controllers.main.TreeController;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.model.event.archive.ArchiveSaveEvent;
import io.github.trimax.venta.editor.model.event.status.StatusSetEvent;
import io.github.trimax.venta.editor.utils.ArchiveFileUtil;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArchiveSaveListener implements AbstractListener<ArchiveSaveEvent> {
    private final TreeController treeController;
    private final Context context;

    @Override
    @Subscribe
    @SneakyThrows
    public void handle(@NonNull final ArchiveSaveEvent event) {
        context.setTree(context.getGroupSelected(), treeController.getRoot());

        // Serialized before the file is opened, so a failure doesn't truncate the previous save
        final var json = ArchiveFileUtil.toJson(context.getTrees());
        Files.writeString(event.file().toPath(), json, StandardCharsets.UTF_8);

        EventUtil.post(new StatusSetEvent("Archive saved to %s", event.file().getAbsoluteFile()));
    }
}
