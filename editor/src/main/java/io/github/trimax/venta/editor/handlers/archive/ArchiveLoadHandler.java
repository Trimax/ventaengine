package io.github.trimax.venta.editor.handlers.archive;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import io.github.trimax.venta.editor.model.dto.ArchiveDTO;
import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.tree.TreeItemListener;
import io.github.trimax.venta.editor.utils.DialogUtil;
import io.github.trimax.venta.editor.utils.TreeUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class ArchiveLoadHandler implements EventHandler<ActionEvent> {
    private final TreeView<Item> tree;
    private final TreeItemListener listener;
    private final Label status;

    @Override
    public void handle(final ActionEvent event) {
        final var stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        DialogUtil.showFileOpen("Please choose a resource to add", this::load, stage,
                Map.of("Archive files (*.json)", List.of("*.json")));
    }

    private void load(final File file) {
        try {
            setTree(new Gson().fromJson(Files.readString(file.toPath(), StandardCharsets.UTF_8), ArchiveDTO.class));
        } catch (final IOException e) {
            status.setText("Can't read archive `" + file.getAbsolutePath()+ "`");
        }
    }

    private void setTree(final ArchiveDTO archive) {
        TreeUtil.initialize(tree, listener);
        status.setText("Archive loaded");


    }
}
