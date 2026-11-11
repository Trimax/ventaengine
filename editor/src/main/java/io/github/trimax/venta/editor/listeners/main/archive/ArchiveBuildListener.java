package io.github.trimax.venta.editor.listeners.main.archive;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.context.Context;

import io.github.trimax.venta.editor.controllers.main.TreeController;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.model.event.archive.ArchiveBuildEvent;
import io.github.trimax.venta.editor.model.event.status.StatusSetEvent;
import io.github.trimax.venta.editor.utils.ArchiveFileUtil;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.FileOutputStream;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArchiveBuildListener implements AbstractListener<ArchiveBuildEvent> {
    private final TreeController treeController;
    private final Context context;

    @Override
    @Subscribe
    @SneakyThrows
    public void handle(@NonNull final ArchiveBuildEvent event) {
        context.setTree(context.getGroupSelected(), treeController.getRoot());

        try (final var out = new FileOutputStream(event.file())) {
            ArchiveFileUtil.build(out, context.getTrees());
        }

        EventUtil.post(new StatusSetEvent("Archive built and saved to %s", event.file().getAbsoluteFile()));
    }
}
