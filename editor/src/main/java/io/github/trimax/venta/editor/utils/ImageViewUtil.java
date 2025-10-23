package io.github.trimax.venta.editor.utils;

import java.awt.image.BufferedImage;

import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ImageViewUtil {
    public void setImage(@NonNull final BufferedImage image, @NonNull final ImageView view) {
        final int width = image.getWidth();
        final int height = image.getHeight();

        final WritableImage fxImage = new WritableImage(width, height);
        final PixelWriter writer = fxImage.getPixelWriter();
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                writer.setColor(x, y, ColorUtil.extract(image, x, y).toColor());

        view.setImage(fxImage);
    }
}
