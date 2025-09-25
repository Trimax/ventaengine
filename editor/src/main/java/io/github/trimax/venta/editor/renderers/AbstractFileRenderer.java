package io.github.trimax.venta.editor.renderers;

import io.github.trimax.venta.engine.model.common.resource.Resource;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.io.File;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractFileRenderer {
    @NonNull
    protected final TreeItem<Resource> node;

    @NonNull
    private final VBox panel;

    private Resource getValue() {
        return node.getValue();
    }

    protected final void add(@NonNull final Node... node) {
        panel.getChildren().addAll(node);
    }

    public abstract void render(@NonNull final File file);
}
