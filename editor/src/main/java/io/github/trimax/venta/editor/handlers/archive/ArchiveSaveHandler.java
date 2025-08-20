package io.github.trimax.venta.editor.handlers.archive;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import io.github.trimax.venta.core.model.common.Node;
import io.github.trimax.venta.editor.model.dto.ArchiveDTO;
import io.github.trimax.venta.editor.model.dto.MetaDTO;
import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.utils.DialogUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@AllArgsConstructor
public final class ArchiveSaveHandler implements EventHandler<ActionEvent> {
    private final TreeView<Item> tree;
    private final Label status;

    @Override
    public void handle(final ActionEvent event) {
        final var stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        DialogUtil.showFileSave("Please choose a file to save an archive", this::save, stage,
                Map.of("Archive files (*.json)", List.of("*.json")));
    }

    @SneakyThrows
    private void save(final File file) {
        try (final var writer = new FileWriter(file)) {
            new Gson().toJson(new ArchiveDTO(new MetaDTO(UUID.randomUUID().toString(), System.currentTimeMillis()),
                    createTreeNode(tree.getRoot())), writer);
        }

        status.setText("File saved to " + file.getAbsoluteFile());
    }

    private Node<String> createTreeNode(final TreeItem<Item> node) {
        return new Node<>(node.getValue().name(), node.getValue().reference(),
                node.getChildren().stream().map(this::createTreeNode).toList());
    }
}
