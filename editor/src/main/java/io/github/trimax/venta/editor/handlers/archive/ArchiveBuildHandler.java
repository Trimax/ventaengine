package io.github.trimax.venta.editor.handlers.archive;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

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
public final class ArchiveBuildHandler implements EventHandler<ActionEvent> {
    private final TreeView<Item> tree;
    private final Label status;

    @Override
    public void handle(final ActionEvent event) {
        final var stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        DialogUtil.showFileSave("Please choose a file to save built archive", this::build, stage,
                Map.of("Binary archive files (*.vea)", List.of("*.vea")));
    }

    @SneakyThrows
    private void build(final File file) {
        try (final var out = new DataOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file))))) {
            writeNode(out, tree.getRoot());
        }

        status.setText("Archive built and saved to " + file.getAbsoluteFile());
    }

    @SneakyThrows
    private void writeNode(final DataOutputStream out, final TreeItem<Item> node) {
        final var item = node.getValue();
        out.writeUTF(item.name());
        out.writeBoolean(item.type().isContainer());

        if (item.type().isContainer()) {
            out.writeInt(node.getChildren().size());
            for (final var child : node.getChildren())
                writeNode(out, child);

            return;
        }

        if (!item.hasExistingReference()) {
            out.writeInt(0);
            return;
        }

        final var bytes = Files.readAllBytes(Path.of(item.reference()));
        out.writeInt(bytes.length);
        out.write(bytes);
    }
}
