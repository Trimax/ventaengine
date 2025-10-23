package io.github.trimax.venta.editor.model.event.mixer;

import io.github.trimax.venta.editor.enums.Channel;
import io.github.trimax.venta.editor.enums.TextureSlot;
import io.github.trimax.venta.editor.model.event.AbstractEvent;
import javafx.scene.image.ImageView;

public record TexturePickEvent(TextureSlot textureSlot,
                               Channel channel,
                               ImageView imgChannel,
                               int value) implements AbstractEvent {
}
