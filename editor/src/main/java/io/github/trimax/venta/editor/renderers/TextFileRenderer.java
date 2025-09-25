package io.github.trimax.venta.editor.renderers;

import io.github.trimax.venta.engine.model.common.resource.Item;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

public final class TextFileRenderer extends AbstractFileRenderer {
    public TextFileRenderer(@NonNull final TreeItem<Item> node, @NonNull final VBox panel) {
        super(node, panel);
    }

    @Override
    @SneakyThrows
    public void render(@NonNull final File file) {
        final var text = new TextArea(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
        text.setPrefRowCount(50);
        text.setEditable(false);
        text.setWrapText(true);

        add(text);
    }
}
