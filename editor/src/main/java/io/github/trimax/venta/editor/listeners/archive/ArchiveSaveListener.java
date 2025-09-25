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
import io.github.trimax.venta.editor.model.event.archive.ArchiveSaveEvent;
import io.github.trimax.venta.editor.model.event.status.StatusSetEvent;
import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.engine.model.common.resource.Item;
import javafx.scene.control.TreeItem;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import one.util.streamex.StreamEx;

import java.io.FileWriter;
import java.util.UUID;
import java.util.function.Function;

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

        try (final var writer = new FileWriter(event.file())) {
            final var groups = StreamEx.of(GroupType.values()).toMap(Function.identity(), this::transform);
            new Gson().toJson(new ArchiveDTO(UUID.randomUUID().toString(), groups), writer);
        }

        EventUtil.post(new StatusSetEvent("Archive saved to %s", event.file().getAbsoluteFile()));
    }

    private Node<Item> transform(@NonNull final GroupType type) {
        return transform(context.getTree(type));
    }

    private Node<Item> transform(final TreeItem<Item> node) {
        return new Node<>(node.getValue().name(), node.getValue(), StreamEx.of(node.getChildren()).map(this::transform).toList());
    }
}
