package io.github.trimax.venta.editor.listeners.main.archive;

import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.context.Context;
import io.github.trimax.venta.editor.controllers.main.ToolbarController;
import io.github.trimax.venta.editor.controllers.main.TreeController;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.model.dto.ArchiveDTO;
import io.github.trimax.venta.editor.model.event.archive.ArchiveLoadEvent;
import io.github.trimax.venta.editor.model.event.status.StatusSetEvent;
import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.engine.model.common.resource.Resource;
import javafx.scene.control.TreeItem;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Slf4j
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
        final var archiveDTO = new Gson().fromJson(Files.readString(event.file().toPath(), StandardCharsets.UTF_8), ArchiveDTO.class);
        for (final var type : GroupType.values())
            context.setTree(type, convert(archiveDTO.getGroup(type)));

        toolbarController.selectGroup(GroupType.AudioSource);
        treeController.getTree().setRoot(context.getTree(GroupType.AudioSource));
        context.setGroupSelected(GroupType.AudioSource);

        EventUtil.post(new StatusSetEvent("Archive loaded from %s", event.file().getAbsolutePath()));
    }

    public TreeItem<Resource> convert(@NonNull final Node<Resource> node) {
        final var vertex = new TreeItem<>(node.value());
        vertex.getChildren().addAll(StreamEx.of(node.children()).map(this::convert).toList());

        return vertex;
    }
}
