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
import io.github.trimax.venta.editor.utils.DialogUtil;
import io.github.trimax.venta.engine.model.common.resource.Resource;
import one.util.streamex.StreamEx;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArchiveBuildListener implements AbstractListener<ArchiveBuildEvent> {
    private final TreeController treeController;
    private final Context context;

    @Override
    @Subscribe
    public void handle(@NonNull final ArchiveBuildEvent event) {
        context.setTree(context.getGroupSelected(), treeController.getRoot());

        final var trees = context.getTrees();
        try (final var out = new FileOutputStream(event.file())) {
            ArchiveFileUtil.build(out, trees);
        } catch (final Exception e) {
            log.error("Can't build archive {}", event.file().getAbsoluteFile(), e);
            EventUtil.post(new StatusSetEvent("Archive is not built: %s", e.getMessage()));
            DialogUtil.showError(String.format("Can't build the archive %s:%n%s", event.file().getAbsoluteFile(), e.getMessage()));
            return;
        }

        final var missing = ArchiveFileUtil.findMissingFiles(trees);
        if (missing.isEmpty()) {
            EventUtil.post(new StatusSetEvent("Archive built and saved to %s", event.file().getAbsoluteFile()));
            return;
        }

        // The archive is built, but these resources are empty in it
        EventUtil.post(new StatusSetEvent("Archive built with %d missing files", missing.size()));
        DialogUtil.showError(String.format("These files are not found and are not packed:%n%s",
                StreamEx.of(missing).map(Resource::reference).joining(System.lineSeparator())));
    }
}
