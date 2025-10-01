package io.github.trimax.venta.editor.controllers.mixer;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.editor.handlers.mixer.TextureSelectHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Control;
import javafx.scene.control.Spinner;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SelectorController {
    private final TextureSelectHandler textureSelectHandler;

    @FXML
    public void onTextureSelect(final ActionEvent event) {
       textureSelectHandler.handle(event);
    }

    @FXML
    public void onChannelSelectRed(final ActionEvent ignored) {
        getSpinner((Control) ignored.getSource()).setDisable(true);
    }

    @FXML
    public void onChannelSelectGreen(final ActionEvent ignored) {
        getSpinner((Control) ignored.getSource()).setDisable(true);
    }

    @FXML
    public void onChannelSelectBlue(final ActionEvent ignored) {
        getSpinner((Control) ignored.getSource()).setDisable(true);
    }

    @FXML
    public void onChannelSelectAlpha(final ActionEvent ignored) {
        getSpinner((Control) ignored.getSource()).setDisable(true);
    }

    @FXML
    public void onChannelSelectValue(final ActionEvent ignored) {
        System.out.println("Event: " + ignored);

        getSpinner((Control) ignored.getSource()).setDisable(false);
    }

    private Spinner<Integer> getSpinner(@NonNull final Control control) {
        return (Spinner<Integer>) control.getParent().getParent().lookup("#txtValue");
    }
}
