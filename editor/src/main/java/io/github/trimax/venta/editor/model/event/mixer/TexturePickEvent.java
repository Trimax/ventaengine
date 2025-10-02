package io.github.trimax.venta.editor.model.event.mixer;

import io.github.trimax.venta.editor.enums.Channel;
import io.github.trimax.venta.editor.model.event.AbstractEvent;
import javafx.scene.image.ImageView;

public record TexturePickEvent(Channel channel, ImageView imgTexture, ImageView imgChannel, int value) implements AbstractEvent {
}
