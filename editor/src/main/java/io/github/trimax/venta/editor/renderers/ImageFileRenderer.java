package io.github.trimax.venta.editor.renderers;

import io.github.trimax.venta.engine.model.common.resource.Item;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import lombok.NonNull;

import java.io.File;

public final class ImageFileRenderer extends AbstractFileRenderer {
    public ImageFileRenderer(final TreeItem<Item> node, final VBox panel) {
        super(node, panel);
    }

    @Override
    public void render(@NonNull final File file) {
        final var imageView = new ImageView(file.toURI().toString());
        imageView.setFitWidth(imageView.getImage().getWidth());
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(Math.min(imageView.getImage().getWidth(), 1024));

        final var image = imageView.getImage();
        final var labelTextureParameters = new Label(String.format("Width: %d; Height: %d", (int) image.getWidth(), (int) image.getHeight()));

        add(labelTextureParameters, imageView);
    }
}
