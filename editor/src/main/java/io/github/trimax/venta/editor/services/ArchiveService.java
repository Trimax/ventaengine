package io.github.trimax.venta.editor.services;

import com.google.gson.Gson;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.definitions.Group;
import io.github.trimax.venta.editor.events.status.StatusSetEvent;
import io.github.trimax.venta.editor.model.dto.ArchiveDTO;
import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.utils.TreeUtil;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import one.util.streamex.StreamEx;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArchiveService {
    public void reset(@NonNull final TreeView<Item> tree) {
        TreeUtil.initialize(tree);

        EventUtil.post(new StatusSetEvent("New archive created"));
    }

    @SneakyThrows
    public void save(@NonNull final File file, @NonNull final TreeItem<Item> root) {
        try (final var writer = new FileWriter(file)) {
            new Gson().toJson(new ArchiveDTO(UUID.randomUUID().toString(), TreeUtil.createNode(root)), writer);
        }

        EventUtil.post(new StatusSetEvent("Archive saved to %s", file.getAbsoluteFile()));
    }

    @SneakyThrows
    public void load(@NonNull final File file, @NonNull final TreeItem<Item> root) {
        final var archiveDTO = new Gson().fromJson(Files.readString(file.toPath(), StandardCharsets.UTF_8), ArchiveDTO.class);
        StreamEx.of(Group.values()).forEach(group -> loadGroup(root, group, archiveDTO));

        EventUtil.post(new StatusSetEvent("Archive loaded from %s", file.getAbsolutePath()));
    }

    @SneakyThrows
    public void build(@NonNull final File file, @NonNull final TreeItem<Item> root) {
        try (final var out = new DataOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file))))) {
            writeNode(out, root, null);
        }

        EventUtil.post(new StatusSetEvent("Archive built and saved to %s", file.getAbsoluteFile()));
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

    @SneakyThrows
    private void writeNode(final DataOutputStream out, final TreeItem<Item> node, final String type) {
        final var item = node.getValue();
        out.writeUTF(item.name().toLowerCase());

        final String currentType = getType(type, item);
        out.writeUTF(currentType);

        out.writeBoolean(item.hasExistingReference());
        if (item.hasExistingReference()) {
            final var bytes = Files.readAllBytes(Path.of(item.reference()));
            out.writeInt(bytes.length);
            out.write(bytes);
        }

        out.writeInt(node.getChildren().size());
        for (final var child : node.getChildren())
            writeNode(out, child, currentType);
    }

    private String getType(final String currentType, final Item item) {
        return !item.deletable() ? item.name() : currentType;
    }
}
