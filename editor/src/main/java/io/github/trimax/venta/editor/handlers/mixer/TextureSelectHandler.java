package io.github.trimax.venta.editor.handlers.mixer;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.model.event.mixer.TextureSelectEvent;
import io.github.trimax.venta.editor.utils.DialogUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.image.ImageView;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.File;
import java.util.List;
import java.util.Map;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextureSelectHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(final ActionEvent event) {
        DialogUtil.showFileOpen("Please choose an image to load", file -> load(file, event),
                Map.of("PNG files (*.png)", List.of("*.png"),
                        "JPG files (*.png)", List.of("*.jpg", "*.jpeg"),
                        "TGA files (*.tga)", List.of("*.tga")));
    }

    private void load(final File file, final ActionEvent event) {
        EventUtil.post(new TextureSelectEvent(file, getTexture((Control) event.getSource()), getChannel((Control) event.getSource())));
    }

    private ImageView getTexture(@NonNull final Control control) {
        return (ImageView) control.getParent().lookup("#imgTexture");
    }

    private ImageView getChannel(@NonNull final Control control) {
        return (ImageView) control.getParent().lookup("#imgChannel");
    }
}
