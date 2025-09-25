package io.github.trimax.venta.editor.listeners.archive;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.controllers.TreeController;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.model.event.archive.ArchiveBuildEvent;
import io.github.trimax.venta.editor.model.event.status.StatusSetEvent;
import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.model.tree.ItemType;
import javafx.scene.control.TreeItem;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArchiveBuildListener implements AbstractListener<ArchiveBuildEvent> {
    private final TreeController treeController;

    @Override
    @Subscribe
    @SneakyThrows
    public void handle(@NonNull final ArchiveBuildEvent event) {
        try (final var out = new DataOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(event.file()))))) {
            writeNode(out, treeController.getRoot(), null);
        }

        EventUtil.post(new StatusSetEvent("Archive built and saved to %s", event.file().getAbsoluteFile()));
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
        return item.type() == ItemType.Group ? item.name() : currentType;
    }
}
