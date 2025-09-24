package io.github.trimax.venta.editor.listeners.archive;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.controllers.TreeController;
import io.github.trimax.venta.editor.events.archive.ArchiveNewEvent;
import io.github.trimax.venta.editor.events.status.StatusSetEvent;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.utils.TreeUtil;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArchiveNewListener implements AbstractListener<ArchiveNewEvent> {
    private final TreeController treeController;

    @Override
    @Subscribe
    public void handle(@NonNull final ArchiveNewEvent event) {
        TreeUtil.initialize(treeController.getTree());

        EventUtil.post(new StatusSetEvent("New archive created"));
    }
}
