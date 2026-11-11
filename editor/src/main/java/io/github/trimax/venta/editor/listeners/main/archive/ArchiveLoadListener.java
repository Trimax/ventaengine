package io.github.trimax.venta.editor.listeners.main.archive;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.context.Context;
import io.github.trimax.venta.editor.controllers.main.ToolbarController;
import io.github.trimax.venta.editor.controllers.main.TreeController;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.model.event.archive.ArchiveLoadEvent;
import io.github.trimax.venta.editor.model.event.status.StatusSetEvent;
import io.github.trimax.venta.editor.utils.ArchiveFileUtil;
import io.github.trimax.venta.engine.enums.GroupType;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArchiveLoadListener implements AbstractListener<ArchiveLoadEvent> {
    private final ToolbarController toolbarController;
    private final TreeController treeController;
    private final Context context;

    @Override
    @Subscribe
    @SneakyThrows
    public void handle(@NonNull final ArchiveLoadEvent event) {
        final var groups = ArchiveFileUtil.fromJson(Files.readString(event.file().toPath(), StandardCharsets.UTF_8));

        // Groups missing in the file start empty instead of keeping the trees of the previous archive
        context.reset();
        groups.forEach(context::setTree);

        toolbarController.selectGroup(GroupType.AudioSource);
        treeController.getTree().setRoot(context.getTree(GroupType.AudioSource));
        context.setGroupSelected(GroupType.AudioSource);

        EventUtil.post(new StatusSetEvent("Archive loaded from %s", event.file().getAbsolutePath()));
    }
}
