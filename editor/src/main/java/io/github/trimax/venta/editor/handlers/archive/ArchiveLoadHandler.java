package io.github.trimax.venta.editor.handlers.archive;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;
import io.github.trimax.venta.core.model.common.Node;
import io.github.trimax.venta.editor.definitions.Element;
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
        } catch (final IOException e) {
            status.setText("Can't read archive `" + file.getAbsolutePath() + "`");
        }
    }

    private void loadTree(final ArchiveDTO archive) {
        TreeUtil.initialize(tree, listener);
        StreamEx.of(Group.values()).forEach(group -> loadGroup(findNode(tree.getRoot(), group.name()),
                Objects.requireNonNull(archive.getGroup(group.name()))));

        status.setText("Archive loaded");
    }

    private void loadGroup(final TreeItem<Item> node, final Node<String> group) {
        System.out.println("Loading group: " + group.name() + " into node: " + node.getValue().name());

        if (group.hasChildren()) {
            final var newGroup = new TreeItem<>(new Item(group.name()));
            node.getChildren().add(newGroup);
            StreamEx.of(group.children()).forEach(child -> loadGroup(newGroup, child));
            return;
        }

        node.setValue(new Item(group.name(), Element.File.getIcon(), group.value()));
    }

    private TreeItem<Item> findNode(final TreeItem<Item> parent, final String name) {
        return StreamEx.of(parent.getChildren())
                .filter(child -> child.getValue().name().equals(name))
                .findAny()
                .orElse(null);
    }
}
