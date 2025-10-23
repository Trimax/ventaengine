package io.github.trimax.venta.editor.model.event.mixer;

import java.io.File;

import io.github.trimax.venta.editor.enums.TextureSlot;
import io.github.trimax.venta.editor.model.event.AbstractEvent;
import javafx.scene.image.ImageView;

public record TextureSelectEvent(File file,
                                 TextureSlot textureSlot,
                                 ImageView imgTexture,
                                 ImageView imgChannel) implements AbstractEvent {
}
