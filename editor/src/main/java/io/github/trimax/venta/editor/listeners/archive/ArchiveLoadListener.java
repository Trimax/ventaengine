package io.github.trimax.venta.editor.listeners.archive;

import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.context.Context;
import io.github.trimax.venta.editor.controllers.TreeController;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.model.dto.ArchiveDTO;
import io.github.trimax.venta.editor.model.event.archive.ArchiveLoadEvent;
import io.github.trimax.venta.editor.model.event.status.StatusSetEvent;
import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.engine.enums.ResourceType;
import javafx.scene.control.TreeItem;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import one.util.streamex.StreamEx;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArchiveLoadListener implements AbstractListener<ArchiveLoadEvent> {
    private final TreeController treeController;
    private final Context context;

    @Override
    @Subscribe
    @SneakyThrows
    public void handle(@NonNull final ArchiveLoadEvent event) {
        final var archiveDTO = new Gson().fromJson(Files.readString(event.file().toPath(), StandardCharsets.UTF_8), ArchiveDTO.class);
        for (final var type : ResourceType.values())
            context.setTree(type, convert(archiveDTO.getGroup(type)));

        treeController.getTree().setRoot(context.getTree(ResourceType.AudioSource));
        context.setGroupSelected(ResourceType.AudioSource);

        EventUtil.post(new StatusSetEvent("Archive loaded from %s", event.file().getAbsolutePath()));
    }

    public TreeItem<Item> convert(@NonNull final Node<String> node) {
        final var item = node.hasValue() ? Item.asResource(node.name(), node.value()) : Item.asFolder(node.name());

        final var vertex = new TreeItem<>(item);
        vertex.getChildren().addAll(StreamEx.of(node.children()).map(this::convert).toList());

        return vertex;
    }
}
