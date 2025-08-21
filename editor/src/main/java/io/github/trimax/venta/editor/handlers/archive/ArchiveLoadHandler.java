package io.github.trimax.venta.editor.handlers.archive;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.editor.definitions.Group;
import io.github.trimax.venta.editor.model.dto.ArchiveDTO;
import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.tree.TreeItemListener;
import io.github.trimax.venta.editor.utils.DialogUtil;
import io.github.trimax.venta.editor.utils.TreeUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import one.util.streamex.StreamEx;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
public final class ArchiveLoadHandler implements EventHandler<ActionEvent> {
    private final TreeView<Item> tree;
    private final TreeItemListener listener;
    private final Label status;

    @Override
    public void handle(final ActionEvent event) {
        final var stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        DialogUtil.showFileOpen("Please choose a resource to add", this::load, stage,
                Map.of("Archive files (*.json)", List.of("*.json")));
    }

    private void load(final File file) {
        try {
            loadTree(new Gson().fromJson(Files.readString(file.toPath(), StandardCharsets.UTF_8), ArchiveDTO.class));
        } catch (final IOException | JsonParseException e) {
            status.setText("Can't read archive `" + file.getAbsolutePath() + "`");
        }
    }

    private void loadTree(final ArchiveDTO archive) {
        TreeUtil.initialize(tree, listener);
        StreamEx.of(Group.values()).forEach(group -> loadGroup(tree.getRoot(), group, archive));

        status.setText("Archive loaded");
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
