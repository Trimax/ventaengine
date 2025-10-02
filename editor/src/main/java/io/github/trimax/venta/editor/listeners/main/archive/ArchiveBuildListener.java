package io.github.trimax.venta.editor.listeners.main.archive;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.context.Context;
import io.github.trimax.venta.editor.controllers.main.TreeController;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.model.event.archive.ArchiveBuildEvent;
import io.github.trimax.venta.editor.model.event.status.StatusSetEvent;
import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.engine.model.common.resource.Resource;
import javafx.scene.control.TreeItem;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import one.util.streamex.StreamEx;

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
    private final Context context;

    @Override
    @Subscribe
    @SneakyThrows
    public void handle(@NonNull final ArchiveBuildEvent event) {
        context.setTree(context.getGroupSelected(), treeController.getRoot());

        try (final var out = new DataOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(event.file()))))) {
            StreamEx.of(GroupType.values()).forEach(group -> writeGroup(out, group));
        }

        EventUtil.post(new StatusSetEvent("Archive built and saved to %s", event.file().getAbsoluteFile()));
    }

    @SneakyThrows
    private void writeGroup(@NonNull final DataOutputStream out, @NonNull final GroupType type) {
        out.writeUTF(type.name());
        writeNode(out, context.getTree(type));
    }

    @SneakyThrows
    private void writeNode(@NonNull final DataOutputStream out, final TreeItem<Resource> node) {
        final var item = node.getValue();
        out.writeUTF(item.name().toLowerCase());

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
