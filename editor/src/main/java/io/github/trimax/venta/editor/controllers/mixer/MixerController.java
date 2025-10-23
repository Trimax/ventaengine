package io.github.trimax.venta.editor.controllers.mixer;

import java.util.List;
import java.util.Map;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.editor.context.MixerContext;
import io.github.trimax.venta.editor.utils.DialogUtil;
import io.github.trimax.venta.editor.utils.ImageUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MixerController {
    private final MixerContext mixerContext;

    @Getter
    @FXML private ImageView imgMix;

    @FXML
    public void onTextureSave(final ActionEvent ignored) {
        DialogUtil.showFileSave("Please choose a file to save texture",
                file -> ImageUtil.write(file, mixerContext.getMixedImage()),
                Map.of("PNG files (*.png)", List.of("*.png")));
    }
}
