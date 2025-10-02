package io.github.trimax.venta.editor.controls;

import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.enums.Channel;
import io.github.trimax.venta.editor.model.event.mixer.TexturePickEvent;
import io.github.trimax.venta.editor.model.event.mixer.TextureSelectEvent;
import io.github.trimax.venta.editor.utils.DialogUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Spinner;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Map;

public final class SelectorControl extends VBox {
    @FXML private Spinner<Integer> txtValue;
    @FXML private ImageView imgTexture;

    @Getter
    @FXML private ImageView imgChannel;

    @SneakyThrows
    public SelectorControl() {
        final var loader = new FXMLLoader(getClass().getResource("/tools/mixer/selector.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        loader.load();
    }

    @FXML
    public void onTextureSelect(final ActionEvent ignored) {
        DialogUtil.showFileOpen("Please choose an image to load",
                file -> EventUtil.post(new TextureSelectEvent(file, imgTexture, imgChannel)),
                Map.of("Images", List.of("*.png", "*.jpg", "*.jpeg", "*.tga")));
    }

    @FXML
    public void onChannelSelectRed(final ActionEvent ignored) {
        txtValue.setDisable(true);
        EventUtil.post(new TexturePickEvent(Channel.Red, imgTexture, imgChannel, 0));
    }

    @FXML
    public void onChannelSelectGreen(final ActionEvent ignored) {
        txtValue.setDisable(true);
        EventUtil.post(new TexturePickEvent(Channel.Green, imgTexture, imgChannel, 0));
    }

    @FXML
    public void onChannelSelectBlue(final ActionEvent ignored) {
        txtValue.setDisable(true);
        EventUtil.post(new TexturePickEvent(Channel.Blue, imgTexture, imgChannel, 0));
    }

    @FXML
    public void onChannelSelectAlpha(final ActionEvent ignored) {
        txtValue.setDisable(true);
        EventUtil.post(new TexturePickEvent(Channel.Alpha, imgTexture, imgChannel, 0));
    }

    @FXML
    public void onChannelSelectValue(final ActionEvent ignored) {
        txtValue.setDisable(false);
        EventUtil.post(new TexturePickEvent(Channel.Value, imgTexture, imgChannel, txtValue.getValue()));
    }

    @FXML
    public void onSpinnerValueChanged() {
        EventUtil.post(new TexturePickEvent(Channel.Value, imgTexture, imgChannel, txtValue.getValue()));
    }
}