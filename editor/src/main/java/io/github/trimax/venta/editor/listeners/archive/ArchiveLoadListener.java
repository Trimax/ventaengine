package io.github.trimax.venta.editor.listeners.archive;

import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.controllers.TreeController;
import io.github.trimax.venta.editor.definitions.Group;
import io.github.trimax.venta.editor.events.archive.ArchiveLoadEvent;
import io.github.trimax.venta.editor.events.status.StatusSetEvent;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.model.dto.ArchiveDTO;
import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.utils.TreeUtil;
import javafx.scene.control.TreeItem;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import one.util.streamex.StreamEx;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArchiveLoadListener implements AbstractListener<ArchiveLoadEvent> {
    private final TreeController treeController;

    @Override
    @Subscribe
    @SneakyThrows
    public void handle(@NonNull final ArchiveLoadEvent event) {
        TreeUtil.initialize(treeController.getTree());

        final var archiveDTO = new Gson().fromJson(Files.readString(event.file().toPath(), StandardCharsets.UTF_8), ArchiveDTO.class);
        StreamEx.of(Group.values()).forEach(group -> loadGroup(treeController.getRoot(), group, archiveDTO));

        EventUtil.post(new StatusSetEvent("Archive loaded from %s", event.file().getAbsolutePath()));
    }

    private void loadGroup(final TreeItem<Item> node, final Group group, final ArchiveDTO archive) {
        loadGroup(findNode(node, group.name()), Objects.requireNonNull(archive.getGroup(group)));
    }

    private void loadGroup(final TreeItem<Item> node, final Node<String> group) {
        final var item = group.hasChildren() ? Item.asGroup(group.name()) : Item.asResource(group.name(), group.value());
        if (node.getValue() != null && node.getValue().deletable())
            node.setValue(item);

        for (final var child : group.children()) {
            final var childNode = new TreeItem<>(
                    child.hasValue()
                            ? Item.asResource(child.name(), child.value())
                            : Item.asGroup(child.name()));

            node.getChildren().add(childNode);

            if (child.hasChildren())
                loadGroup(childNode, child);
        }
    }

    private TreeItem<Item> findNode(final TreeItem<Item> parent, final String name) {
        return StreamEx.of(parent.getChildren())
                .filter(child -> child.getValue().name().equals(name))
                .findAny()
                .orElse(null);
    }
}
