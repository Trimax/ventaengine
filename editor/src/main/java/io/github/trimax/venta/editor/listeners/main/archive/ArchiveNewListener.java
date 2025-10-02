package io.github.trimax.venta.editor.listeners.main.archive;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.context.Context;
import io.github.trimax.venta.editor.controllers.main.ToolbarController;
import io.github.trimax.venta.editor.controllers.main.TreeController;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.model.event.archive.ArchiveNewEvent;
import io.github.trimax.venta.editor.model.event.status.StatusSetEvent;
import io.github.trimax.venta.editor.utils.TreeUtil;
import io.github.trimax.venta.engine.enums.GroupType;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArchiveNewListener implements AbstractListener<ArchiveNewEvent> {
    private final ToolbarController toolbarController;
    private final TreeController treeController;
    private final Context context;

    @Override
    @Subscribe
    public void handle(@NonNull final ArchiveNewEvent event) {
        toolbarController.selectGroup(GroupType.AudioSource);
        TreeUtil.initialize(treeController.getTree());
        context.reset();

        EventUtil.post(new StatusSetEvent("New archive created"));
    }
}
