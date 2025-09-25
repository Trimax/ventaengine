package io.github.trimax.venta.editor.listeners.group;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.context.Context;
import io.github.trimax.venta.editor.controllers.TreeController;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.model.event.group.GroupSelectEvent;
import io.github.trimax.venta.editor.model.event.status.StatusSetEvent;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class GroupSelectListener implements AbstractListener<GroupSelectEvent> {
    private final TreeController treeController;
    private final Context context;

    @Override
    @Subscribe
    public void handle(@NonNull final GroupSelectEvent event) {
        context.setTree(context.getGroupSelected(), treeController.getRoot());

        treeController.getTree().setRoot(context.getTree(event.type()));
        context.setGroupSelected(event.type());

        EventUtil.post(new StatusSetEvent("Group `%s` selected", event.type().name()));
    }
}
