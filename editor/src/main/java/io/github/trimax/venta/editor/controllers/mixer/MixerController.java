package io.github.trimax.venta.editor.controllers.mixer;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.editor.controls.SelectorControl;
import io.github.trimax.venta.editor.utils.DialogUtil;
import io.github.trimax.venta.editor.utils.ImageUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MixerController {
    @FXML private SelectorControl picker1;
    @FXML private SelectorControl picker2;
    @FXML private SelectorControl picker3;
    @FXML private SelectorControl picker4;

    @Getter
    @FXML private ImageView imgMix;

    @FXML
    public void onTextureSave(final ActionEvent ignored) {
        DialogUtil.showFileSave("Please choose a file to save texture", file -> ImageUtil.write(file, imgMix.getImage()),
                Map.of("PNG files (*.png)", List.of("*.png")));
    }

    public ImageView getRedChannel() {
        return picker1.getImgChannel();
    }

    public ImageView getGreenChannel() {
        return picker2.getImgChannel();
    }

    public ImageView getBlueChannel() {
        return picker3.getImgChannel();
    }

    public ImageView getAlphaChannel() {
        return picker4.getImgChannel();
    }
}
