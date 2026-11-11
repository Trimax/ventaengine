package io.github.trimax.venta.editor.utils;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import com.google.gson.Gson;

import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.editor.model.dto.ArchiveDTO;
import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.engine.model.common.resource.Resource;
import javafx.scene.control.TreeItem;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import one.util.streamex.StreamEx;

/** Converts editor trees to the project JSON and to the binary archive the engine loads */
@UtilityClass
public final class ArchiveFileUtil {
    private static final Gson GSON = new Gson();

    public String toJson(@NonNull final Map<GroupType, TreeItem<Resource>> groups) {
        final var nodes = new LinkedHashMap<GroupType, Node<Resource>>();
        groups.forEach((type, tree) -> nodes.put(type, toNode(tree)));

        return GSON.toJson(new ArchiveDTO(UUID.randomUUID().toString(), nodes));
    }

    /** Returns the groups present in the project JSON */
    public Map<GroupType, TreeItem<Resource>> fromJson(@NonNull final String json) {
        final var archive = GSON.fromJson(json, ArchiveDTO.class);

        final var groups = new LinkedHashMap<GroupType, TreeItem<Resource>>();
        for (final var type : GroupType.values()) {
            final var group = archive.getGroup(type);
            if (group != null)
                groups.put(type, toTreeItem(group));
        }

        return groups;
    }

    public Node<Resource> toNode(@NonNull final TreeItem<Resource> item) {
        return new Node<>(item.getValue().name(), item.getValue(), StreamEx.of(item.getChildren()).map(ArchiveFileUtil::toNode).toList());
    }

    public TreeItem<Resource> toTreeItem(@NonNull final Node<Resource> node) {
        final var item = new TreeItem<>(node.value());
        if (node.hasChildren())
            item.getChildren().addAll(StreamEx.of(node.children()).map(ArchiveFileUtil::toTreeItem).toList());

        return item;
    }

    /** Writes the groups in the binary archive format: group name, then the group tree with file contents */
    @SneakyThrows
    public void build(@NonNull final OutputStream output, @NonNull final Map<GroupType, TreeItem<Resource>> groups) {
        try (final var out = new DataOutputStream(new GZIPOutputStream(new BufferedOutputStream(output)))) {
            for (final var group : groups.entrySet()) {
                out.writeUTF(group.getKey().name());
                writeNode(out, group.getValue(), group.getKey().getArchiveFolder());
            }
        }
    }

    @SneakyThrows
    private void writeNode(@NonNull final DataOutputStream out, final TreeItem<Resource> node, @NonNull final String name) {
        final var item = node.getValue();
        out.writeUTF(name);

        out.writeBoolean(item.hasExistingReference());
        if (item.hasExistingReference()) {
            final var bytes = Files.readAllBytes(Path.of(item.reference()));
            out.writeInt(bytes.length);
            out.write(bytes);
        }

        out.writeInt(node.getChildren().size());
        for (final var child : node.getChildren())
            writeNode(out, child, child.getValue().name());
    }
}
