package io.github.trimax.venta.editor.renderers;

import java.io.File;

import org.apache.commons.io.FileUtils;

import io.github.trimax.venta.engine.model.common.resource.Resource;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;
import lombok.NonNull;

/** Shows only the size of binary files (sounds, fonts) that can't be previewed as text */
public final class BinaryFileRenderer extends AbstractFileRenderer {
    public BinaryFileRenderer(@NonNull final TreeItem<Resource> node, @NonNull final VBox panel) {
        super(node, panel);
    }

    @Override
    public void render(@NonNull final File file) {
        add(new Label("Size: " + FileUtils.byteCountToDisplaySize(file.length())));
    }
}
