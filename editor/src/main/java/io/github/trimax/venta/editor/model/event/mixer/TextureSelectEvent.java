package io.github.trimax.venta.editor.model.event.mixer;

import io.github.trimax.venta.editor.model.event.AbstractEvent;
import javafx.scene.image.ImageView;

import java.io.File;

public record TextureSelectEvent(File file, ImageView imageView, ImageView channelView) implements AbstractEvent {
}
