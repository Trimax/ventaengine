package io.github.trimax.venta.editor.handlers.archive;

import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.utils.DialogUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

@AllArgsConstructor
public final class ArchiveBuildHandler implements EventHandler<ActionEvent> {
    private final TreeView<Item> tree;
    private final Label status;

    @Override
    public void handle(final ActionEvent event) {
        DialogUtil.showFileSave("Please choose a file to save built archive", this::build,
                tree.getScene().getWindow(), Map.of("Binary archive files (*.vea)", List.of("*.vea")));
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

        out.writeBoolean(item.hasExistingReference());
        if (item.hasExistingReference()) {
            final var bytes = Files.readAllBytes(Path.of(item.reference()));
            out.writeInt(bytes.length);
            out.write(bytes);
        }

        out.writeInt(node.getChildren().size());
        for (final var child : node.getChildren())
            writeNode(out, child);
    }
}
