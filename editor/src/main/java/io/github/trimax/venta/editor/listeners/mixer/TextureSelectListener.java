package io.github.trimax.venta.editor.listeners.mixer;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.editor.controllers.mixer.SelectorController;
import io.github.trimax.venta.editor.enums.Channel;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.model.event.mixer.TextureSelectEvent;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextureSelectListener implements AbstractListener<TextureSelectEvent> {
    private final SelectorController selectorController;

    @Override
    @Subscribe
    public void handle(@NonNull final TextureSelectEvent event) {
        event.imageView().setImage(new Image(event.file().toURI().toString()));
        updateChannelPreview(event.imageView(), event.channelView(), Channel.Red, 0);
    }

    private void updateChannelPreview(ImageView imgTexture, ImageView imgChannel, Channel channel, Integer value) {
        Image base = imgTexture.getImage();
        if (base == null) return;

        final var width = (int) base.getWidth();
        final var height = (int) base.getHeight();

        WritableImage result = new WritableImage(width, height);
        PixelReader reader = base.getPixelReader();
        PixelWriter writer = result.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final var color = reader.getColor(x, y);

                double v;
                switch (channel) {
                    case Red   -> v = color.getRed();
                    case Green -> v = color.getGreen();
                    case Blue  -> v = color.getBlue();
                    case Alpha -> v = color.getOpacity();
                    case Value -> v = (value != null ? value / 255.0 : 0.0);
                    default      -> v = 0.0;
                }


                writer.setColor(x, y, new Color(v, v, v, 1.0));
            }
        }

        imgChannel.setImage(result);
    }
}
