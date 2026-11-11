package io.github.trimax.venta.editor.utils;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.zip.GZIPOutputStream;

import com.google.gson.Gson;

import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.editor.model.dto.ArchiveDTO;
import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.engine.model.common.resource.Resource;
import io.github.trimax.venta.engine.enums.ResourceType;
import javafx.scene.control.TreeItem;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;

/** Converts editor trees to the project JSON and to the binary archive the engine loads */
@UtilityClass
public final class ArchiveFileUtil {
    private static final Gson GSON = new Gson();

    /** File references are stored relative to the project directory, so the project can be moved or shared */
    public String toJson(@NonNull final Map<GroupType, TreeItem<Resource>> groups, @NonNull final Path projectDirectory) {
        final var nodes = new LinkedHashMap<GroupType, Node<Resource>>();
        groups.forEach((type, tree) -> nodes.put(type, mapReferences(toNode(tree), reference -> relativize(projectDirectory, reference))));

        return GSON.toJson(new ArchiveDTO(UUID.randomUUID().toString(), nodes));
    }

    /** Returns the groups present in the project JSON; relative file references are resolved against the project directory */
    public Map<GroupType, TreeItem<Resource>> fromJson(@NonNull final String json, @NonNull final Path projectDirectory) {
        final var archive = GSON.fromJson(json, ArchiveDTO.class);

        final var groups = new LinkedHashMap<GroupType, TreeItem<Resource>>();
        for (final var type : GroupType.values()) {
            final var group = archive.getGroup(type);
            if (group != null)
                groups.put(type, toTreeItem(mapReferences(group, reference -> resolve(projectDirectory, reference))));
        }

        return groups;
    }

    /** Resources of the trees whose files don't exist, so they are not packed into the archive */
    public List<Resource> findMissingFiles(@NonNull final Map<GroupType, TreeItem<Resource>> groups) {
        final var missing = new ArrayList<Resource>();
        groups.values().forEach(tree -> collectMissingFiles(tree, missing));

        return missing;
    }

    private void collectMissingFiles(final TreeItem<Resource> node, final List<Resource> missing) {
        final var resource = node.getValue();
        if (resource.type() == ResourceType.File && !resource.hasExistingReference())
            missing.add(resource);

        node.getChildren().forEach(child -> collectMissingFiles(child, missing));
    }

    private Node<Resource> mapReferences(final Node<Resource> node, final UnaryOperator<String> mapper) {
        final var resource = node.value();
        final var mapped = resource != null && resource.type() == ResourceType.File && StringUtils.isNotBlank(resource.reference())
                ? new Resource(resource.type(), resource.name(), mapper.apply(resource.reference()))
                : resource;
        final var children = node.hasChildren() ? StreamEx.of(node.children()).map(child -> mapReferences(child, mapper)).toList() : node.children();

        return new Node<>(node.name(), mapped, children);
    }

    private String relativize(final Path projectDirectory, final String reference) {
        try {
            return projectDirectory.toAbsolutePath().normalize().relativize(Path.of(reference).toAbsolutePath().normalize())
                    .toString().replace('\\', '/');
        } catch (final IllegalArgumentException e) {
            // E.g. another drive on Windows: there is no relative path
            return reference;
        }
    }

    private String resolve(final Path projectDirectory, final String reference) {
        final var path = Path.of(reference);
        return path.isAbsolute() ? reference : projectDirectory.resolve(path).normalize().toString();
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
